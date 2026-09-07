#!/usr/bin/env node
/**
 * Собирает romanian-daily-offline.html — один автономный файл:
 * стили, скрипт и весь словарь внутри. Открывается двойным кликом
 * (в том числе с file://), интернет не нужен.
 *
 * Запуск: npm run build:offline
 */
import { readFile, writeFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");
const read = (name) => readFile(join(ROOT, name), "utf8");
const PACK_MAX = 12;

const [html, css, app] = await Promise.all([read("index.html"), read("styles.css"), read("app.js")]);

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

// Экранируем «<», чтобы данные не могли закрыть тег <script>
const data = `window.__ROMANIAN_DAILY_WORDS__=${JSON.stringify(words).replace(/</g, "\\u003c")};`;

const banner = `<!--\n  Romanian Daily — офлайн-сборка от ${new Date().toISOString().slice(0, 10)}.\n  Сгенерировано scripts/build-offline.mjs: не редактируйте файл вручную,\n  правки вносите в index.html / app.js / words-partN.json и пересобирайте.\n-->\n`;

const out = html
  .replace('<link rel="stylesheet" href="./styles.css">', `<style>\n${css}\n</style>`)
  .replace('  <link rel="manifest" href="./manifest.json">\n', "")
  .replace(
    '  <script src="./app.js"></script>',
    `  <script>\n${data}\n  </script>\n  <script>\n${app}\n  </script>`
  )
  .replace("<title>", `${banner}<title>`);

if (out.includes('href="./styles.css"') || out.includes('src="./app.js"')) {
  console.error("Не удалось встроить стили или скрипт — проверьте разметку index.html");
  process.exit(1);
}

await writeFile(join(ROOT, "romanian-daily-offline.html"), out);
console.log(`romanian-daily-offline.html готов: ${words.length} слов, ${(out.length / 1024).toFixed(0)} КБ`);
