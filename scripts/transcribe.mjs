#!/usr/bin/env node
/**
 * Практическая транскрипция румынского текста кириллицей (для русскоязычных).
 * Инструмент для подготовки данных, в браузере не используется.
 *
 *   node ./scripts/transcribe.mjs "Bună! Ce mai faci?"
 *   node ./scripts/transcribe.mjs --check    — сравнить с написанными вручную
 *   node ./scripts/transcribe.mjs --apply    — переписать транскрипции в данных
 */
import { readFile, writeFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");

const RO_VOWELS = "aeiouăâî";
const isVowel = (ch) => RO_VOWELS.includes(ch);

/* Слова-исключения: заимствования, которые читаются не по румынским правилам. */
const EXCEPTIONS = {
  site: "сайт",
  email: "имеил",
  emailul: "имеилул",
  teatru: "театру",
  exemplu: "екземплу",
  computer: "компьютер",
  manager: "менеджер",
  weekend: "уикенд",
  business: "бизнес",
  shopping: "шопинг",
};

const normalize = (text) =>
  String(text ?? "")
    .toLowerCase()
    .replace(/ş/g, "ș")
    .replace(/ţ/g, "ț")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/-/g, ""); // e-mail → email

/* Согласные, после которых конечное -i звучит мягко и пишется как «ь». */
const SOFT = new Set(["ч", "ж", "ц", "к", "г"]);

/** Транскрипция одного румынского слова (без пробелов). */
function word2ru(rawWord) {
  const w = normalize(rawWord).replace(/[^a-zăâîșț]/g, "");
  if (!w) return "";
  if (EXCEPTIONS[w]) return EXCEPTIONS[w];

  const out = [];
  const at = (i) => w[i] ?? "";
  const push = (s) => out.push(s);
  const last = () => out[out.length - 1] ?? "";

  for (let i = 0; i < w.length; i += 1) {
    const ch = at(i);
    const nx = at(i + 1);
    const nn = at(i + 2);
    const prev = i > 0 ? at(i - 1) : "";
    const prevIsVowel = prev ? "aeiouăâî".includes(prev) : false;
    const endOfWord = i === w.length - 1;

    /* --- ch / gh --- */
    if ((ch === "c" || ch === "g") && nx === "h") {
      push(ch === "c" ? "к" : "г");
      i += 1;
      continue;
    }

    /* --- cea / gea: «чай», «жакэ» --- */
    if ((ch === "c" || ch === "g") && nx === "e" && nn === "a") {
      push(ch === "c" ? "ча" : "жа");
      i += 2;
      continue;
    }

    /* --- c / g перед e, i --- */
    if (ch === "c" || ch === "g") {
      const soft = ch === "c" ? "ч" : "ж";
      const hard = ch === "c" ? "к" : "г";
      if (nx === "e") {
        push(soft + "е");
        i += 1;
        continue;
      }
      if (nx === "i") {
        if (!nn) {
          push(soft + "ь"); // faci → фачь, fugi → фужь
        } else if ("aeiouăâî".includes(nn)) {
          push(soft); // niciodată → ничодатэ (i немое)
        } else {
          push(soft + "и"); // cine → чине
        }
        i += 1;
        continue;
      }
      push(hard);
      continue;
    }

    /* --- ea: дифтонг [e̯a] --- */
    if (ch === "e" && nx === "a") {
      push(at(i + 2) ? "я" : "еа"); // dimineața → диминяца, avea → авеа
      i += 1;
      continue;
    }

    /* --- гласные --- */
    if (ch === "ă") { push("э"); continue; }
    if (ch === "â" || ch === "î") { push("ы"); continue; }
    if (ch === "e") { push("е"); continue; }
    if (ch === "a") { push("а"); continue; }
    if (ch === "o") { push("о"); continue; }
    if (ch === "u") { push("у"); continue; }

    if (ch === "i") {
      const nextIsVowel = nx && "aeiouăâî".includes(nx);
      if (i === 0 && nx === "u") { push("ю"); i += 1; continue; }        // iubire → юбире
      if (endOfWord) {
        if (prevIsVowel) { push("й"); continue; }                       // doi → дой, pui → пуй
        if (prev === "t" && (at(i - 2) === "s" || at(i - 2) === "ș")) { push("ь"); continue; } // ești → ешть
        push(SOFT.has(last()) ? "ь" : "и");                              // găti → гэти, ochi → окь
        continue;
      }
      if (nextIsVowel && (i === 0 || prevIsVowel)) { push("й"); continue; } // baie → байе, ieri → йери
      if (prev === "â" || prev === "î") { push("й"); continue; }         // câine → кыйне
      if (!nextIsVowel && prevIsVowel) { push("й"); continue; }          // haină → хайнэ, aici → айчь
      push("и");
      continue;
    }

    /* --- согласные --- */
    const map = {
      b: "б", d: "д", f: "ф", h: "х", j: "ж", k: "к", l: "л", m: "м", n: "н",
      p: "п", q: "кв", r: "р", s: "с", ș: "ш", t: "т", ț: "ц", v: "в", w: "в",
      x: "кс", y: "и", z: "з",
    };
    if (map[ch]) {
      push(map[ch]);
      if (ch === "ț" && nx === "i" && i + 1 === w.length - 1) {
        out[out.length - 1] = "ць"; // vorbiți → ворбиць
        i += 1;
      }
      continue;
    }
  }

  return out
    .join("")
    .replace(/йь$/, "ь")
    .replace(/(.)\1(?=[кпстфхцчшщ])/g, "$1"); // accident → акчидент
}

/** Транскрипция фразы: по словам, знаки препинания выбрасываем. */
export function ro2ru(text) {
  return normalize(text)
    .split(/(\s+|[^\s a-zăâîșț]+)/)
    .map((chunk) => {
      if (!chunk || !/[a-zăâîșț]/.test(chunk)) return " ";
      return word2ru(chunk);
    })
    .join("")
    .replace(/\s+/g, " ")
    .trim();
}

/* ------------------------------------------------------------------- режимы */

async function loadPacks() {
  const files = [];
  for (let i = 1; i <= 12; i += 1) {
    const name = `words-part${i}.json`;
    try {
      files.push([name, JSON.parse(await readFile(join(ROOT, name), "utf8"))]);
    } catch {
      break;
    }
  }
  return files;
}

const args = process.argv.slice(2);

if (args[0] === "--check") {
  const packs = await loadPacks();
  const words = packs.flatMap(([, p]) => p);
  const mine = words.filter((w) => w.day > 30); // здесь транскрипции писаны вручную

  let exact = 0;
  const diff = [];
  for (const w of mine) {
    const auto = ro2ru(w.word);
    const hand = (w.transcription || "").replace(/-/g, "").trim();
    if (auto === hand) exact += 1;
    else diff.push(`${w.word}: рука [${hand}] / авто [${auto}]`);
  }
  console.log(`Слова моей части (${mine.length}): совпало ${exact}, расхождений ${diff.length}`);
  console.log(diff.slice(0, 40).join("\n"));

  let eExact = 0;
  let eTotal = 0;
  const eDiff = [];
  for (const w of mine) {
    for (const ex of w.examples ?? []) {
      if (!ex.transcription || ex.transcription === w.transcription) continue;
      eTotal += 1;
      const auto = ro2ru(ex.ro);
      const hand = ex.transcription.replace(/-/g, "").trim();
      if (auto === hand) eExact += 1;
      else eDiff.push(`${ex.ro}\n    рука [${hand}]\n    авто [${auto}]`);
    }
  }
  console.log(`\nПримеры (${eTotal}): совпало ${eExact}, расхождений ${eTotal - eExact}`);
  console.log(eDiff.slice(0, 20).join("\n"));
} else if (args[0] === "--apply") {
  const packs = await loadPacks();
  let wordsFixed = 0;
  let examplesFixed = 0;

  for (const [, pack] of packs) {
    for (const w of pack) {
      const plain = (w.transcription || "").replace(/-/g, "").trim();
      const auto = ro2ru(w.word);
      if (auto && auto !== plain) {
        w.transcription = auto;
        wordsFixed += 1;
      } else if (plain !== w.transcription) {
        w.transcription = plain;
        wordsFixed += 1;
      }
      for (const ex of w.examples ?? []) {
        const cur = (ex.transcription || "").trim();
        const auto2 = ro2ru(ex.ro);
        if (!auto2) continue;
        if (!cur || cur === w.transcription || cur === plain || cur.includes("-") || !/[а-яё]/i.test(cur)) {
          if (auto2 !== cur) {
            ex.transcription = auto2;
            examplesFixed += 1;
          }
        }
      }
    }
  }

  for (const [name, pack] of packs) {
    await writeFile(join(ROOT, name), `${JSON.stringify(pack, null, 1)}\n`);
  }
  console.log(`Обновлено транскрипций: слов ${wordsFixed}, примеров ${examplesFixed}`);
} else {
  console.log(ro2ru(args.join(" ")));
}
