#!/usr/bin/env node
/**
 * Собирает romanian-daily-offline.html — один автономный файл:
 * стили, скрипт и все данные (слова, грамматика, квиз, фразы) внутри.
 * Открывается двойным кликом, работает по file:// и без интернета.
 *
 * Запуск: npm run build:offline
 */
import { readFile, writeFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");
const read = (name) => readFile(join(ROOT, name), "utf8");
const PACK_MAX = 12;

const [html, css, app, appSrs] = await Promise.all([
  read("index.html"),
  read("styles.css"),
  read("app.js"),
  read("app-srs.js"),
]);

// Склеиваем все паки в один массив
const words = [];
for (let i = 1; i <= PACK_MAX; i += 1) {
  try {
    words.push(...JSON.parse(await read(`words-part${i}.json`)));
  } catch {
    break; // паки кончились
  }
}
if (!words.length) {
  console.error("Не найден ни один words-partN.json");
  process.exit(1);
}

const data = {
  words,
  grammar: JSON.parse(await read("grammar.json")),
  quiz: JSON.parse(await read("quiz.json")),
  phrases: JSON.parse(await read("phrases.json")),
};

// Экранируем «<», чтобы данные не могли закрыть тег <script>
const payload = `window.__ROMANIAN_DAILY_DATA__=${JSON.stringify(data).replace(/</g, "\\u003c")};`;

const banner = `<!--
  Romanian Daily — офлайн-сборка от ${new Date().toISOString().slice(0, 10)}.
  Сгенерировано scripts/build-offline.mjs: не редактируйте файл вручную,
  правки вносите в index.html / app.js / styles.css / *.json и пересобирайте.
-->
`;

const out = html
  .replace('  <link rel="stylesheet" href="./styles.css">\n', `  <style>\n${css}\n  </style>\n`)
  .replace('  <link rel="manifest" href="./manifest.json">\n', "")
  .replace(
    '  <script src="./app.js"></script>',
    `  <script>\n${payload}\n  </script>\n  <script>\n${app}\n  </script>`
  )
  .replace(
    '  <script src="./app-srs.js"></script>',
    `  <script>\n${appSrs}\n  </script>`
  )
  .replace("<!DOCTYPE html>", `${banner}<!DOCTYPE html>`);

if (out.includes('href="./styles.css"') || out.includes('src="./app.js"') || out.includes('src="./app-srs.js"')) {
  console.error("Не удалось встроить стили или скрипты — проверьте разметку index.html");
  process.exit(1);
}

await writeFile(join(ROOT, "romanian-daily-offline.html"), out);
console.log(
  `romanian-daily-offline.html готов: ${words.length} слов, ${data.quiz.length} вопросов, ` +
    `${data.phrases.length} фраз, ${(out.length / 1024).toFixed(0)} КБ`
);
