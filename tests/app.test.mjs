#!/usr/bin/env node
/**
 * Дымовой тест интерфейса: прогоняет приложение в jsdom
 * (главная → урок → словарь → фразы → грамматика и тест → офлайн-сборка → ошибка загрузки).
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
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

/** Поднимает приложение в jsdom; fetch читает файлы с диска. */
async function bootApp({ html, fetchImpl, inline = false, waitForText } = {}) {
  const { JSDOM } = createRequire(import.meta.url)("jsdom");
  const spoken = [];
  const dom = new JSDOM(html ?? (await read("index.html")), {
    url: "http://localhost/",
    runScripts: "dangerously",
    beforeParse(window) {
      installPolyfills(window, { fetchImpl, spoken });
    },
  });
  const { window } = dom;
  if (!inline) {
    // Вставляем как обычный <script>: иначе let-переменные приложения
    // останутся внутри eval и к ним не достучаться из теста
    const tag = window.document.createElement("script");
    tag.textContent = await read("app.js");
    window.document.body.appendChild(tag);
  }

  // Ждём, пока асинхронно подгрузятся данные курса
  const deadline = Date.now() + 8000;
  while (Date.now() < deadline) {
    const body = window.document.body.textContent;
    if (waitForText ? body.includes(waitForText) : /День \d|Данные не загрузились/.test(body)) break;
    await sleep(50);
  }
  await sleep(100);
  return { window, doc: window.document, spoken };
}

function installPolyfills(window, { fetchImpl, spoken }) {
  window.matchMedia = () => ({ matches: false });
  Object.defineProperty(window.navigator, "userAgent", { value: "Mozilla/5.0 (X11; Linux x86_64) Chrome" });
  Object.defineProperty(window.navigator, "serviceWorker", { value: { register: async () => {} } });
  window.confirm = () => true;
  window.alert = () => {};
  window.SpeechSynthesisUtterance = class {
    constructor(text) {
      this.text = text;
    }
  };
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
}

/* ------------------------------------------------------------------ helpers */

const makeHelpers = (doc, window) => {
  const text = () => doc.body.textContent.replace(/\s+/g, " ");
  const buttons = () => [...doc.querySelectorAll("button")];
  const has = (label) => buttons().some((b) => b.textContent.trim().includes(label));
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
    has,
    click,
    count: (sel) => doc.querySelectorAll(sel).length,
    word: () => doc.querySelector(".word h2")?.textContent.trim(),
    clickSel: (sel) => doc.querySelector(sel)?.click(),
    fire: (el, type) => el.dispatchEvent(new window.Event(type, { bubbles: true })),
    go: (view) => window.eval(`state.view=${JSON.stringify(view)}; render();`),
  };
};

/* ---------------------------------------------------------------- сценарий */

const { window, doc, spoken } = await bootApp();
const { text, click, has, count, word, clickSel, fire, go } = makeHelpers(doc, window);

check(text().includes("День 1"), "Главная: «День 1»");
check(has("Начать день"), "Главная: кнопка «Начать день»");
check(!doc.querySelector('script[src*="cdn.tailwindcss.com"]'), "Внешних CDN нет");

// «Начать день» запускает поток дня; порядок шагов иногда перемешивается (фича),
// поэтому вид после клика не фиксируем
click("Начать день");
const flowView = window.eval("state.view");
check(flowView !== "home", `«Начать день» запускает шаг дня (${flowView})`);

// урок слов открываем детерминированно — кнопкой «+» в нижнем меню
go("home");
clickSel('[data-act="start-daily"]');
check(text().includes("1/10"), "Урок: счётчик 1/10");
const first = word();
check(!!first && first.length > 0, `Урок: показывается слово «${first}»`);
check(!doc.querySelector(".word .ru"), "Урок: перевод скрыт до нажатия");

click("Показать перевод");
const translation = doc.querySelector(".word .ru")?.textContent.trim();
check(!!translation, `Урок: перевод открывается («${translation}»)`);

clickSel("[data-speak]");
check(spoken.length > 0, `Озвучка произносит «${spoken[0]}»`);

// «Знаю» у слова (data-act) → появляется «Далее»
clickSel('[data-act="know"]');
await sleep(50);
check(has("Далее") || has("Завершить"), "Урок: после отметки «Знаю» доступен переход дальше");

go("dictionary");
check(text().includes("Слова") || count(".drow") > 0, "Словарь: список слов");
const totalRows = count(".drow");
check(totalRows > 100, `Словарь: строк ${totalRows}`);
const search = doc.querySelector("#search-box");
if (search) {
  search.focus();
  search.value = "bună";
  fire(search, "input");
  const filtered = count(".drow");
  check(filtered > 0 && filtered < totalRows, `Поиск «bună»: ${filtered} из ${totalRows}`);
  check(doc.activeElement?.id === "search-box", "Фокус остаётся в поле поиска после перерисовки");
} else {
  check(false, "Словарь: не найдено поле поиска #search-box");
}

go("home");
click("Все фразы");
check(text().includes("Предложения"), "Фразы: экран открывается");
check(count(".ex") >= 100, `Фразы: показано ${count(".ex")}`);

go("grammar");
check(text().includes("Грамматика"), "Грамматика: экран открывается");
check(text().includes("Приветствие и вежливость"), "Грамматика: заметка дня 1 на месте");
check(text().includes("День 49"), "Грамматика: заметки есть и на добавленные дни (31-49)");
click("Пройти тест");
const opts = count("[data-gopt]");
check(opts >= 2, `Тест по грамматике: ${opts} варианта ответа`);
clickSel("[data-gopt]");
check(count(".right") >= 1, "Тест: после ответа подсвечивается правильный вариант");

/* ---------------------------------------------------------- офлайн-сборка */

const offlineHtml = await read("romanian-daily-offline.html").catch(() => null);
if (offlineHtml) {
  const off = await bootApp({
    html: offlineHtml,
    inline: true,
    fetchImpl: async () => {
      throw new Error("file:// — fetch недоступен");
    },
  });
  const offText = off.doc.body.textContent.replace(/\s+/g, " ");
  check(offText.includes("День 1"), "Офлайн-сборка: рендерится из встроенных данных");
  check(off.doc.querySelectorAll('link[rel="stylesheet"], script[src]').length === 0, "Офлайн-сборка: без внешних ссылок");
  off.window.eval('state.view="phrases"; render();');
  check(off.doc.querySelectorAll(".ex").length >= 100, "Офлайн-сборка: фразы внутри файла");
} else {
  console.log("· romanian-daily-offline.html не найден — пропуск (npm run build:offline)");
}

/* ------------------------------------------------- обработка ошибок загрузки */

const broken = await bootApp({
  fetchImpl: async () => ({ ok: false, status: 404, json: async () => { throw new Error("404"); } }),
});
check(broken.doc.body.textContent.includes("Данные не загрузились"), "При 404 показывается экран ошибки, а не белый лист");

console.log(failed ? `\nПровалено проверок: ${failed}` : "\nВсе проверки интерфейса пройдены");
process.exit(failed ? 1 : 0);
