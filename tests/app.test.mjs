#!/usr/bin/env node
/**
 * Дымовой тест интерфейса: прогоняет приложение в jsdom
 * (главная → урок → повторение → словарь → сброс прогресса → обработка ошибки).
 *
 * Запуск: npm test   (нужны dev-зависимости: npm install)
 */
import { readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";
import { createRequire } from "node:module";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");
const read = (name) => readFile(join(ROOT, name), "utf8");

let failed = 0;
const check = (ok, message) => {
  if (!ok) failed += 1;
  console.log(`${ok ? "✓" : "✗"} ${message}`);
};
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

/** Поднимает приложение в jsdom; fetch читает файлы с диска. */
async function bootApp({ fetchImpl } = {}) {
  const { JSDOM } = createRequire(import.meta.url)("jsdom");
  const dom = new JSDOM(await read("index.html"), { url: "http://localhost/", runScripts: "dangerously" });
  const { window } = dom;

  window.matchMedia = () => ({ matches: false });
  Object.defineProperty(window.navigator, "userAgent", { value: "Mozilla/5.0 (X11; Linux x86_64) Chrome" });
  Object.defineProperty(window.navigator, "serviceWorker", { value: { register: async () => {} } });
  window.confirm = () => true;
  window.SpeechSynthesisUtterance = class {
    constructor(text) {
      this.text = text;
    }
  };
  const spoken = [];
  window.speechSynthesis = {
    getVoices: () => [{ name: "Romanian", lang: "ro-RO" }],
    cancel() {},
    speak(u) {
      spoken.push(u.text);
    },
    addEventListener() {},
  };
  window.console = { ...console, error: () => {}, warn: () => {} };
  window.fetch =
    fetchImpl ??
    (async (url) => {
      try {
        const body = await read(String(url).replace("./", ""));
        return { ok: true, status: 200, json: async () => JSON.parse(body) };
      } catch {
        return { ok: false, status: 404, json: async () => { throw new Error("404"); } };
      }
    });

  window.eval(await read("app.js"));
  await wait(200);
  return { window, doc: window.document, spoken };
}

/* ------------------------------------------------------------------ helpers */

const makeHelpers = (doc, window) => {
  const text = () => doc.body.textContent.replace(/\s+/g, " ");
  const buttons = () => [...doc.querySelectorAll("button")];
  const has = (label) => buttons().some((x) => x.textContent.trim().includes(label));
  const click = (label) => {
    const b = buttons().find((x) => x.textContent.trim().includes(label));
    if (!b) {
      failed += 1;
      console.log(`✗ Кнопка «${label}» не найдена`);
      return false;
    }
    b.click();
    return true;
  };
  return {
    text,
    click,
    has,
    word: () => doc.querySelector("h2")?.textContent.trim(),
    clickSelector: (sel) => doc.querySelector(sel)?.click(),
    fire: (el, type) => el.dispatchEvent(new window.Event(type, { bubbles: true })),
  };
};

/* ---------------------------------------------------------------- сценарий */

const { window, doc, spoken } = await bootApp();
const { text, click, has, word, clickSelector, fire } = makeHelpers(doc, window);

check(text().includes("День 1"), "Главная: заголовок «День 1»");
check(text().includes("10 слов сегодня"), "Главная: счётчик слов дня");
check(!!doc.querySelector('link[href="./styles.css"]'), "Локальный styles.css подключён");
check(!doc.querySelector('script[src*="cdn.tailwindcss.com"]'), "CDN Tailwind не используется");

click("Начать урок");
check(text().includes("1 / 10"), "Урок: счётчик 1 / 10");
check(text().includes("Показать перевод"), "Урок: перевод скрыт до нажатия");
const first = word();

click("Показать перевод");
check(text().includes("Отметить выученным"), "Урок: после раскрытия доступна отметка");
click("Отметить выученным");
check(text().includes("Выучено"), "Урок: слово отмечено выученным");

clickSelector("[data-speak]");
check(spoken.length === 1 && spoken[0] === first, `Озвучка произносит «${spoken[0]}»`);

click("Далее");
const second = word();
check(first !== second, `Урок: переход к следующему слову (${first} → ${second})`);
click("Назад");
check(word() === first, "Урок: «Назад» возвращает к предыдущему слову");

let guard = 0;
while (guard < 30) {
  guard += 1;
  if (has("Завершить урок")) {
    click("Завершить урок");
    break;
  }
  if (!click("Далее")) break;
}
check(text().includes("Урок пройден"), "Урок завершается тостом «Урок пройден»");
check(/🔥\s*1/.test(doc.querySelector("header").textContent), "Стрик = 1 после первого урока");

const saved = JSON.parse(window.localStorage.getItem("romanian_daily_v2"));
check(saved?.learnedWordIds?.length === 1, "Прогресс записан в localStorage");
check(saved?.streak === 1 && !!saved?.lastStudyDate, "Стрик и дата занятия сохранены");

click("Начать");
const r1 = word();
click("Далее");
const r2 = word();
click("Назад");
const r3 = word();
check(r1 === r3 && r1 !== r2, `Повторение: список не перемешивается заново (${r1} → ${r2} → ${r3})`);

click("Слова");
check(text().includes("Словарь"), "Словарь открывается");
const total = doc.querySelectorAll("[data-toggle]").length;
const search = doc.querySelector("#search");
search.focus();
search.value = "bună";
fire(search, "input");
const filtered = doc.querySelectorAll("[data-toggle]").length;
check(filtered > 0 && filtered < total, `Поиск «bună»: найдено ${filtered} из ${total}`);
check(doc.activeElement?.id === "search", "Фокус остаётся в поле поиска после перерисовки");

click("Главная");
click("Сбросить прогресс");
check(JSON.parse(window.localStorage.getItem("romanian_daily_v2")).learnedWordIds.length === 0, "Сброс прогресса очищает выученные слова");

/* ------------------------------------------------- обработка ошибок загрузки */

const broken = await bootApp({ fetchImpl: async () => ({ ok: false, status: 404, json: async () => { throw new Error("404"); } }) });
check(broken.doc.body.textContent.includes("Не удалось загрузить слова"), "При 404 показывается экран ошибки, а не белый лист");

console.log(failed ? `\nПровалено проверок: ${failed}` : "\nВсе проверки интерфейса пройдены");
process.exit(failed ? 1 : 0);
