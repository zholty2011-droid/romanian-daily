#!/usr/bin/env node
/**
 * Валидация словарных данных (words-part1.json … words-partN.json).
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

/** Та же логика загрузки, что и в app.js: качаем паки, пока файл существует. */
async function loadPacks() {
  const packs = [];
  for (let i = 1; i <= PACK_MAX; i += 1) {
    const name = `words-part${i}.json`;
    let raw;
    try {
      raw = await readFile(join(ROOT, name), "utf8");
    } catch (err) {
      if (err.code === "ENOENT") break;
      throw err;
    }
    try {
      packs.push({ name, words: JSON.parse(raw) });
    } catch (err) {
      fail(`${name}: невалидный JSON — ${err.message}`);
    }
  }
  return packs;
}

function checkWord(word, packName, index, seen) {
  const where = `${packName}[${index}] («${word?.word ?? "?"}»)`;
  const has = (k) => typeof word[k] === "string" && word[k].trim().length > 0;

  if (typeof word !== "object" || word === null || Array.isArray(word)) {
    fail(`${where}: ожидается объект`);
    return null;
  }
  for (const key of ["id", "word", "transcription", "translation"]) {
    if (!has(key)) fail(`${where}: отсутствует или пустое поле «${key}»`);
  }
  if (!Number.isInteger(word.day) || word.day < 1) fail(`${where}: «day» должен быть целым числом ≥ 1`);
  if (word.word && !/^[a-zăâîșțA-ZĂÂÎȘȚ' -]+$/.test(word.word)) {
    warn(`${where}: в слове неожиданные символы — «${word.word}»`);
  }
  for (const key of ["word", "transcription", "translation"]) {
    if (typeof word[key] === "string" && PLACEHOLDERS.test(word[key])) {
      fail(`${where}: в поле «${key}» найден плейсхолдер`);
    }
  }

  if (typeof word.id === "string") {
    if (seen.ids.has(word.id)) fail(`${where}: дубликат id «${word.id}»`);
    seen.ids.add(word.id);
  }
  if (typeof word.word === "string") {
    const key = word.word.trim().toLowerCase();
    if (seen.words.has(key)) fail(`${where}: слово «${word.word}» уже встречается в словаре`);
    seen.words.add(key);
  }

  if (word.examples !== undefined) {
    if (!Array.isArray(word.examples)) {
      fail(`${where}: «examples» должен быть массивом`);
    } else {
      word.examples.forEach((ex, i) => {
        for (const key of ["ro", "transcription", "ru"]) {
          if (typeof ex?.[key] !== "string" || !ex[key].trim()) {
            fail(`${where}: пример #${i + 1} — отсутствует поле «${key}»`);
          }
        }
      });
      if (word.examples.length === 0) warn(`${where}: нет примеров употребления`);
    }
  } else {
    warn(`${where}: нет примеров употребления`);
  }

  return word;
}

const packs = await loadPacks();

if (packs.length === 0) {
  fail("Не найден ни один words-partN.json — приложению нечего показывать");
} else {
  const seen = { ids: new Set(), words: new Set() };
  let total = 0;

  for (const { name, words } of packs) {
    if (!Array.isArray(words)) {
      fail(`${name}: корень файла должен быть массивом слов`);
      continue;
    }
    total += words.length;
    words.forEach((w, i) => checkWord(w, name, i, seen));
  }

  const all = packs.flatMap((p) => (Array.isArray(p.words) ? p.words : []));
  const byDay = new Map();
  for (const w of all) {
    if (Number.isInteger(w?.day)) byDay.set(w.day, (byDay.get(w.day) ?? 0) + 1);
  }
  const days = [...byDay.keys()].sort((a, b) => a - b);

  for (let day = 1; day <= days.length; day += 1) {
    if (!byDay.has(day)) fail(`Пропущен день ${day}: нумерация дней должна идти без разрывов`);
  }
  for (const [day, count] of [...byDay.entries()].sort((a, b) => a[0] - b[0])) {
    if (count !== TARGET_PER_DAY) warn(`День ${day}: ${count} слов (ожидалось ${TARGET_PER_DAY})`);
  }

  console.log(`Проверено паков: ${packs.length}, слов: ${total}, дней: ${days.length}`);
}

for (const w of warnings) console.warn(`⚠︎  ${w}`);
for (const e of errors) console.error(`✗  ${e}`);

if (errors.length) {
  console.error(`\nНайдено ошибок: ${errors.length}`);
  process.exit(1);
}
console.log(`✓ Данные валидны${warnings.length ? ` (предупреждений: ${warnings.length})` : ""}`);
