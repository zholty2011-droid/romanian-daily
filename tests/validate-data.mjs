#!/usr/bin/env node
/**
 * Валидация данных курса:
 *   words-part1..N.json — слова (id, day, word, translation, transcription, examples)
 *   grammar.json        — грамматические заметки по дням
 *   quiz.json           — вопросы грамматического теста
 *   phrases.json        — разговорные фразы
 *
 * Запуск: npm test   (нужен только Node, без зависимостей)
 */
import { readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");
const PACK_MAX = 12;
const TARGET_PER_DAY = 10;
const PLACEHOLDERS = /TODO|FIXME|XXX|\bTBD\b|\?\?\?/i;

const errors = [];
const warnings = [];
const fail = (msg) => errors.push(msg);
const warn = (msg) => warnings.push(msg);

const readJson = async (name) => {
  try {
    return JSON.parse(await readFile(join(ROOT, name), "utf8"));
  } catch (err) {
    if (err.code === "ENOENT") return undefined;
    fail(`${name}: ${err.message}`);
    return undefined;
  }
};

/* --------------------------------------------------------------------- слова */

function checkWord(word, where, seen) {
  const at = `${where} («${word?.word ?? "?"}»)`;
  const has = (k) => typeof word[k] === "string" && word[k].trim().length > 0;

  if (typeof word !== "object" || word === null || Array.isArray(word)) {
    fail(`${at}: ожидается объект`);
    return;
  }
  for (const key of ["id", "word", "transcription", "translation"]) {
    if (!has(key)) fail(`${at}: отсутствует или пустое поле «${key}»`);
  }
  if (!Number.isInteger(word.day) || word.day < 1) fail(`${at}: «day» должен быть целым числом ≥ 1`);
  if (word.word && !/^[a-zăâîșțA-ZĂÂÎȘȚ' -]+$/.test(word.word)) {
    warn(`${at}: в слове неожиданные символы — «${word.word}»`);
  }
  for (const key of ["word", "transcription", "translation"]) {
    if (typeof word[key] === "string" && PLACEHOLDERS.test(word[key])) fail(`${at}: в поле «${key}» плейсхолдер`);
  }
  if (typeof word.id === "string") {
    if (seen.ids.has(word.id)) fail(`${at}: дубликат id «${word.id}»`);
    seen.ids.add(word.id);
  }
  if (typeof word.word === "string") {
    const key = word.word.trim().toLowerCase();
    if (seen.words.has(key)) fail(`${at}: слово «${word.word}» уже есть в курсе`);
    seen.words.add(key);
  }

  if (word.examples !== undefined) {
    if (!Array.isArray(word.examples)) {
      fail(`${at}: «examples» должен быть массивом`);
    } else {
      word.examples.forEach((ex, i) => {
        for (const key of ["ro", "transcription", "ru"]) {
          if (typeof ex?.[key] !== "string" || !ex[key].trim()) fail(`${at}: пример #${i + 1} — пустое поле «${key}»`);
        }
        // Автоген вида «Где autobuz?»: румынское слово осталось в русском переводе
        if (typeof ex?.ru === "string" && /[a-zăâîșț]{3,}/i.test(ex.ru.replace(/e-mail/gi, ""))) {
          fail(`${at}: пример #${i + 1} — перевод не переведён: «${ex.ru}»`);
        }
      });
      if (!word.examples.length) warn(`${at}: нет примеров`);
    }
  } else {
    warn(`${at}: нет примеров`);
  }
}

async function checkWords() {
  const seen = { ids: new Set(), words: new Set() };
  const words = [];
  let packs = 0;

  for (let i = 1; i <= PACK_MAX; i += 1) {
    const name = `words-part${i}.json`;
    const pack = await readJson(name);
    if (pack === undefined) break;
    packs += 1;
    if (!Array.isArray(pack)) {
      fail(`${name}: корень файла должен быть массивом`);
      continue;
    }
    pack.forEach((w, idx) => checkWord(w, `${name}[${idx}]`, seen));
    words.push(...pack);
  }

  if (!packs) {
    fail("Не найден ни один words-partN.json — приложению нечего показывать");
    return [];
  }

  const byDay = new Map();
  for (const w of words) {
    if (Number.isInteger(w?.day)) byDay.set(w.day, (byDay.get(w.day) ?? 0) + 1);
  }
  const days = [...byDay.keys()].sort((a, b) => a - b);
  for (let day = 1; day <= days.length; day += 1) {
    if (!byDay.has(day)) fail(`Пропущен день ${day}: дни должны идти без разрывов`);
  }
  for (const [day, count] of byDay) {
    if (count !== TARGET_PER_DAY) warn(`День ${day}: ${count} слов (обычно ${TARGET_PER_DAY})`);
  }

  console.log(`Слова: ${words.length} в ${packs} паках, дней: ${days.length}`);
  return words;
}

/* ------------------------------------------------------------ грамматика/квиз/фразы */

async function checkGrammar() {
  const g = await readJson("grammar.json");
  if (!g) {
    fail("grammar.json не найден");
    return;
  }
  if (typeof g !== "object" || Array.isArray(g)) {
    fail("grammar.json: ожидается объект { день: [заголовок, …пункты] }");
    return;
  }
  const days = Object.keys(g).map(Number).sort((a, b) => a - b);
  for (const day of days) {
    const block = g[String(day)];
    if (!Array.isArray(block) || !block.length) {
      fail(`grammar.json: день ${day} — ожидается непустой массив строк`);
      continue;
    }
    block.forEach((line, i) => {
      if (typeof line !== "string" || !line.trim()) fail(`grammar.json: день ${day}, строка ${i + 1} пуста`);
    });
  }
  console.log(`Грамматика: заметок на ${days.length} дней`);
}

async function checkQuiz() {
  const q = await readJson("quiz.json");
  if (!q) {
    fail("quiz.json не найден");
    return;
  }
  if (!Array.isArray(q)) {
    fail("quiz.json: ожидается массив вопросов");
    return;
  }
  const ids = new Set();
  for (const [i, item] of q.entries()) {
    const at = `quiz.json[${i}]`;
    if (!item?.id || ids.has(item.id)) fail(`${at}: отсутствует или повторяется id`);
    ids.add(item.id);
    if (typeof item.q !== "string" || !item.q.trim()) fail(`${at}: пустой вопрос`);
    if (!Array.isArray(item.opts) || item.opts.length < 2) fail(`${at}: нужно минимум 2 варианта ответа`);
    if (!Number.isInteger(item.a) || item.a < 0 || item.a >= (item.opts?.length ?? 0)) {
      fail(`${at}: индекс правильного ответа вне диапазона`);
    }
    if (typeof item.why !== "string" || !item.why.trim()) fail(`${at}: нет пояснения «why»`);
    if (new Set(item.opts).size !== item.opts?.length) warn(`${at}: варианты ответа повторяются`);
  }
  console.log(`Квиз: ${q.length} вопросов`);
}

async function checkPhrases() {
  const p = await readJson("phrases.json");
  if (!p) {
    fail("phrases.json не найден");
    return;
  }
  if (!Array.isArray(p)) {
    fail("phrases.json: ожидается массив фраз");
    return;
  }
  const ids = new Set();
  for (const [i, item] of p.entries()) {
    const at = `phrases.json[${i}]`;
    if (!item?.id || ids.has(item.id)) fail(`${at}: отсутствует или повторяется id`);
    ids.add(item.id);
    for (const key of ["ro", "ru"]) {
      if (typeof item[key] !== "string" || !item[key].trim()) fail(`${at}: пустое поле «${key}»`);
    }
    if (item.ru && /[a-zăâîșț]{3,}/i.test(item.ru.replace(/e-mail/gi, ""))) {
      fail(`${at}: перевод не переведён: «${item.ru}»`);
    }
  }
  console.log(`Фразы: ${p.length}`);
}

/* ----------------------------------------------------------------------- итог */

await checkWords();
await checkGrammar();
await checkQuiz();
await checkPhrases();

for (const w of warnings) console.warn(`⚠︎  ${w}`);
for (const e of errors) console.error(`✗  ${e}`);

if (errors.length) {
  console.error(`\nНайдено ошибок: ${errors.length}`);
  process.exit(1);
}
console.log(`✓ Данные валидны${warnings.length ? ` (предупреждений: ${warnings.length})` : ""}`);
