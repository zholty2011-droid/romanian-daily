/* Romanian Daily — офлайн-first PWA: 10 румынских слов в день. */
"use strict";

const STORAGE_KEY = "romanian_daily_v2";
const LEGACY_STORAGE_KEY = "romanian_daily_progress";
const INSTALL_DISMISS_KEY = "romanian_daily_install_dismissed";
const PACK_MAX = 12; // словарь лежит в words-part1.json … words-partN.json
const LESSON_SIZE = 10;
const WORDS_PER_DAY = 10;

/* ------------------------------------------------------------------ утилиты */

const ESCAPES = { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" };
/** Экранирование: данные словаря подставляются в innerHTML. */
const esc = (value) => String(value ?? "").replace(/[&<>"']/g, (c) => ESCAPES[c]);

const todayISO = () => new Date().toISOString().slice(0, 10);

/** Разница в днях между датами YYYY-MM-DD. */
function daysBetween(from, to) {
  const a = Date.parse(`${from}T00:00:00`);
  const b = Date.parse(`${to}T00:00:00`);
  return Number.isNaN(a) || Number.isNaN(b) ? Number.NaN : Math.round((b - a) / 86_400_000);
}

function shuffle(list) {
  const arr = list.slice();
  for (let i = arr.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
  return arr;
}

const plural = (n, one, few, many) => {
  const mod100 = n % 100;
  const mod10 = n % 10;
  if (mod100 >= 11 && mod100 <= 14) return many;
  if (mod10 === 1) return one;
  if (mod10 >= 2 && mod10 <= 4) return few;
  return many;
};

/* -------------------------------------------------------------------- стейт */

const root = document.getElementById("root");

const state = {
  status: "loading", // loading | ready | error
  error: null,
  words: [],
  view: "home", // home | lesson | dictionary
  lessonMode: "daily", // daily | review | all
  lessonList: [], // снимок списка на время урока (иначе повторение перемешивается при каждом рендере)
  lessonIndex: 0,
  revealed: false,
  progress: loadProgress(),
  filter: "all", // all | unlearned
  query: "",
  toast: null,
  deferredPrompt: null,
  showInstall: false,
  isiOS: false,
};

const totalDays = () => state.words.reduce((max, w) => Math.max(max, w.day), 0);
const isLearned = (id) => state.progress.learnedWordIds.includes(id);
const maxDay = () => totalDays();

/* ----------------------------------------------------------------- прогресс */

function defaultProgress() {
  return { learnedWordIds: [], streak: 0, lastStudyDate: null, currentDay: 1, updatedAt: null };
}

function loadProgress() {
  let raw = null;
  try {
    raw = localStorage.getItem(STORAGE_KEY) ?? localStorage.getItem(LEGACY_STORAGE_KEY);
  } catch (e) {
    console.warn("localStorage недоступен, прогресс не сохраняется", e);
  }
  const progress = defaultProgress();
  if (raw) {
    try {
      Object.assign(progress, JSON.parse(raw));
    } catch (e) {
      console.warn("Не удалось прочитать сохранённый прогресс", e);
    }
  }
  if (!Array.isArray(progress.learnedWordIds)) progress.learnedWordIds = [];
  progress.streak = Number(progress.streak) || 0;
  progress.currentDay = Number(progress.currentDay) || 1;
  return progress;
}

function saveProgress() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ ...state.progress, updatedAt: new Date().toISOString() }));
  } catch (e) {
    console.warn("Не удалось сохранить прогресс", e);
    showToast("Прогресс не сохраняется (нет доступа к localStorage)");
  }
}

function setProgress(patch) {
  state.progress = { ...state.progress, ...patch };
  saveProgress();
  render();
}

/** Серия прерывается, если пропущен хотя бы один день. */
function normalizeProgress() {
  const { lastStudyDate } = state.progress;
  if (!lastStudyDate) return;
  const gap = daysBetween(lastStudyDate, todayISO());
  if (Number.isNaN(gap) || gap > 1) state.progress = { ...state.progress, streak: 0 };
  const last = maxDay();
  if (last && state.progress.currentDay > last) state.progress.currentDay = last;
}

function resetProgress() {
  if (!window.confirm("Сбросить весь прогресс? Действие необратимо.")) return;
  state.progress = defaultProgress();
  state.lessonList = [];
  state.lessonIndex = 0;
  state.view = "home";
  saveProgress();
  showToast("Прогресс сброшен");
  render();
}

/* --------------------------------------------------------------------- звук */

let voices = [];

function refreshVoices() {
  try {
    voices = window.speechSynthesis?.getVoices?.() ?? [];
  } catch (e) {
    voices = [];
  }
}

if ("speechSynthesis" in window) {
  refreshVoices();
  // В Chrome список голосов приходит асинхронно.
  window.speechSynthesis.addEventListener?.("voiceschanged", refreshVoices);
}

function speakRomanian(text) {
  const synth = window.speechSynthesis;
  if (!synth || !text) return;
  try {
    synth.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    const voice =
      voices.find((v) => /^ro\b/i.test(v.lang)) || voices.find((v) => /romanian|румын/i.test(v.name));
    if (voice) utterance.voice = voice;
    utterance.lang = voice?.lang ?? "ro-RO";
    utterance.rate = 0.85;
    synth.speak(utterance);
  } catch (e) {
    console.warn("Озвучка недоступна", e);
  }
}

/* -------------------------------------------------------------------- тосты */

let toastTimer = null;

function showToast(message) {
  state.toast = message;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    state.toast = null;
    render();
  }, 2600);
  render();
}

/* --------------------------------------------------------------- загрузка данных */

async function loadWords() {
  const packs = [];
  for (let i = 1; i <= PACK_MAX; i += 1) {
    const name = `words-part${i}.json`;
    const res = await fetch(`./${name}`);
    if (res.status === 404) break; // паки кончились — это нормально
    if (!res.ok) throw new Error(`${name}: сервер ответил ${res.status}`);
    try {
      packs.push(await res.json());
    } catch (e) {
      throw new Error(`${name}: не удалось разобрать JSON`);
    }
  }
  if (!packs.length) throw new Error("Не найден ни один файл words-partN.json");

  const seenIds = new Set();
  return packs
    .flat()
    .filter((w) => w && typeof w.word === "string" && w.word.trim())
    .map((w, i) => ({
      id: String(w.id ?? `w${String(i + 1).padStart(3, "0")}`),
      day: Number(w.day) || 1,
      word: String(w.word).trim(),
      transcription: String(w.transcription ?? "").trim(),
      translation: String(w.translation ?? "").trim(),
      examples: Array.isArray(w.examples)
        ? w.examples
            .filter((ex) => ex && typeof ex.ro === "string")
            .map((ex) => ({
              ro: String(ex.ro).trim(),
              transcription: String(ex.transcription ?? "").trim(),
              ru: String(ex.ru ?? "").trim(),
            }))
        : [],
    }))
    .filter((w) => !seenIds.has(w.id) && seenIds.add(w.id));
}

/* ------------------------------------------------------------------- выборки */

function dailyWords() {
  return state.words.filter((w) => w.day === state.progress.currentDay);
}

function unlearnedWords() {
  return state.words.filter((w) => w.day <= state.progress.currentDay && !isLearned(w.id));
}

function buildLessonList(mode) {
  if (mode === "daily") return dailyWords();
  if (mode === "all") return shuffle(state.words).slice(0, LESSON_SIZE);
  return shuffle(unlearnedWords()).slice(0, LESSON_SIZE);
}

function startLesson(mode) {
  const list = buildLessonList(mode);
  if (!list.length) {
    showToast(mode === "review" ? "Нечего повторять — всё выучено 🎉" : "В этом дне нет слов");
    return;
  }
  state.lessonMode = mode;
  state.lessonList = list;
  state.lessonIndex = 0;
  state.revealed = false;
  state.view = "lesson";
  render();
}

/* ------------------------------------------------------------------ действия */

function toggleLearned(id) {
  const ids = isLearned(id)
    ? state.progress.learnedWordIds.filter((x) => x !== id)
    : [...state.progress.learnedWordIds, id];
  setProgress({ learnedWordIds: ids });
}

function goToWord(index) {
  const words = state.lessonList;
  if (index < 0 || index >= words.length) return;
  state.lessonIndex = index;
  state.revealed = false;
  render();
}

function completeLesson() {
  const today = todayISO();
  const streak = state.progress.lastStudyDate === today ? state.progress.streak : state.progress.streak + 1;
  state.progress = { ...state.progress, lastStudyDate: today, streak };
  state.view = "home";
  state.lessonList = [];
  saveProgress();
  showToast(state.lessonMode === "daily" ? "Урок пройден! 🔥" : "Повторение завершено 👍");
  render();
}

function openNextDay() {
  const next = state.progress.currentDay + 1;
  if (next > maxDay()) {
    showToast("Курс пройден — можно повторять пройденное 🎉");
    return;
  }
  state.progress = { ...state.progress, currentDay: next };
  state.lessonList = [];
  saveProgress();
  render();
}

/* --------------------------------------------------------------------- виды */

function speakerBtn(text, cls) {
  return `<button type="button" data-speak="${esc(encodeURIComponent(text))}" class="${cls}" aria-label="Озвучить: ${esc(text)}">
    <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z"/></svg>
  </button>`;
}

function header() {
  return `<header class="fixed top-0 left-0 right-0 bg-white/85 backdrop-blur-md border-b border-slate-200 z-40 pt-safe">
    <div class="max-w-md mx-auto px-4 py-3 flex justify-between items-center">
      <button type="button" data-go="home" class="flex items-center gap-2" aria-label="На главную">
        <span class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center text-white font-bold">R</span>
        <span class="font-bold text-slate-800 tracking-tight">Romanian Daily</span>
      </button>
      <div class="flex items-center gap-1.5 text-orange-500 font-semibold bg-orange-50 px-3 py-1 rounded-full border border-orange-100"
           title="Дней подряд">
        <span class="text-sm" aria-hidden="true">🔥</span><span class="text-sm">${state.progress.streak}</span>
      </div>
    </div>
  </header>`;
}

function nav() {
  const tab = (view) => (state.view === view ? "text-blue-600" : "text-slate-400");
  return `<nav class="fixed bottom-0 left-0 right-0 bg-white/90 backdrop-blur-lg border-t border-slate-100 px-6 py-3 flex justify-around items-center z-40 pb-safe">
    <button type="button" data-go="home" class="flex flex-col items-center gap-1 ${tab("home")}"><span class="text-[10px] font-black uppercase">Главная</span></button>
    <button type="button" data-act="quick-lesson" class="w-12 h-12 bg-blue-600 rounded-2xl text-white font-black -mt-8 shadow-lg shadow-blue-600/30" aria-label="Начать урок">+</button>
    <button type="button" data-go="dictionary" class="flex flex-col items-center gap-1 ${tab("dictionary")}"><span class="text-[10px] font-black uppercase">Слова</span></button>
  </nav>`;
}

function installBanner() {
  if (!state.showInstall) return "";
  const iosOnly = state.isiOS && !state.deferredPrompt;
  return `<div class="fixed top-20 left-4 right-4 z-50 max-w-md mx-auto bg-slate-900 text-white p-4 rounded-2xl shadow-2xl flex items-start gap-3" role="dialog" aria-label="Установить приложение">
    <div class="flex-1 text-sm">
      <p class="font-bold">Установить на телефон</p>
      <p class="text-slate-300 text-xs mt-1">${iosOnly ? "Safari → Поделиться → На экран «Домой»" : "Работает офлайн, как приложение"}</p>
    </div>
    ${state.deferredPrompt ? `<button type="button" data-act="install" class="bg-blue-600 text-xs font-bold px-3 py-2 rounded-xl shrink-0">Установить</button>` : ""}
    <button type="button" data-act="hide-install" class="text-slate-400 text-lg leading-none px-1" aria-label="Скрыть">×</button>
  </div>`;
}

function toastView() {
  if (!state.toast) return "";
  return `<div class="fixed bottom-24 left-4 right-4 z-50 max-w-md mx-auto">
    <div class="bg-slate-900/95 text-white text-sm font-semibold text-center px-4 py-3 rounded-2xl shadow-xl">${esc(state.toast)}</div>
  </div>`;
}

function loadingView() {
  return `<div class="min-h-screen pt-24 pb-28 px-4 max-w-md mx-auto animate-pulse">
    <div class="h-48 bg-slate-200 rounded-[2rem]"></div>
    <div class="grid grid-cols-3 gap-3 mt-5"><div class="h-20 bg-slate-200 rounded-2xl"></div><div class="h-20 bg-slate-200 rounded-2xl"></div><div class="h-20 bg-slate-200 rounded-2xl"></div></div>
    <p class="text-center text-slate-400 text-sm mt-8">Загружаю слова…</p>
  </div>`;
}

function errorView() {
  return `<div class="min-h-screen pt-24 pb-28 px-4 max-w-md mx-auto flex flex-col items-center justify-center text-center gap-4">
    <div class="text-4xl" aria-hidden="true">📶</div>
    <h2 class="text-xl font-bold">Не удалось загрузить слова</h2>
    <p class="text-sm text-slate-500">${esc(state.error ?? "Неизвестная ошибка")}</p>
    <button type="button" data-act="reload" class="bg-blue-600 text-white font-bold px-6 py-3 rounded-2xl">Попробовать снова</button>
  </div>`;
}

function homeView() {
  const p = state.progress;
  const words = dailyWords();
  const learnedToday = words.filter((w) => isLearned(w.id)).length;
  const finished = words.length > 0 && learnedToday >= words.length;
  const unlearned = unlearnedWords();
  const days = maxDay();
  const pct = words.length ? Math.round((learnedToday / words.length) * 100) : 0;
  const courseDone = !state.words.some((w) => w.day > p.currentDay) && finished;

  let cta;
  if (courseDone) {
    cta = `<button type="button" data-act="start-all" class="mt-6 w-full bg-white text-slate-900 py-4 rounded-2xl font-bold">Повторить всё заново</button>`;
  } else if (finished) {
    cta = `<button type="button" data-act="next-day" class="mt-6 w-full bg-green-600 text-white py-4 rounded-2xl font-bold">Открыть день ${p.currentDay + 1}</button>`;
  } else {
    cta = `<button type="button" data-act="start-daily" class="mt-6 w-full bg-blue-600 text-white py-4 rounded-2xl font-bold">${learnedToday ? "Продолжить урок" : "Начать урок"}</button>`;
  }

  const stat = (value, label) =>
    `<div class="bg-white p-5 rounded-2xl border border-slate-200 text-center">
      <div class="text-xl font-black">${value}</div>
      <div class="text-[10px] text-slate-400 uppercase font-bold mt-0.5">${label}</div>
    </div>`;

  return `<div class="min-h-screen pt-24 pb-28 px-4 max-w-md mx-auto flex flex-col gap-4">
    <section class="bg-slate-900 text-white p-7 rounded-[2rem] shadow-xl shadow-slate-900/20">
      <div class="flex items-start justify-between gap-4">
        <div>
          <p class="text-slate-400 text-[11px] uppercase font-bold tracking-wide">${courseDone ? "Курс пройден 🎉" : "Твой прогресс"}</p>
          <h2 class="text-3xl font-black mt-1">День ${p.currentDay}</h2>
          <p class="text-slate-400 text-sm mt-1">${words.length} ${plural(words.length, "слово", "слова", "слов")} сегодня</p>
        </div>
        <div class="text-right shrink-0">
          <div class="text-2xl font-black">${learnedToday}<span class="text-slate-500 text-lg">/${words.length}</span></div>
          <div class="text-[10px] text-slate-500 uppercase font-bold">выучено</div>
        </div>
      </div>
      <div class="mt-6">
        <div class="w-full bg-slate-800 h-2.5 rounded-full overflow-hidden">
          <div class="bg-blue-500 h-full transition-all duration-500" style="width:${pct}%"></div>
        </div>
      </div>
      ${cta}
    </section>

    <div class="grid grid-cols-3 gap-3">
      ${stat(p.learnedWordIds.length, "выучено")}
      ${stat(state.words.length, "всего слов")}
      ${stat(`${p.currentDay}/${days}`, "дней")}
    </div>

    ${unlearned.length ? `<section class="bg-white border border-slate-200 p-5 rounded-2xl flex items-center justify-between">
      <div>
        <h3 class="font-bold">Повторение</h3>
        <p class="text-[11px] text-slate-400">${unlearned.length} ${plural(unlearned.length, "слово", "слова", "слов")} ждут повтора</p>
      </div>
      <button type="button" data-act="start-review" class="px-5 py-2.5 bg-slate-900 text-white text-xs font-bold rounded-xl">Начать</button>
    </section>` : ""}

    <button type="button" data-act="reset" class="mt-2 self-center text-xs text-slate-400 underline">Сбросить прогресс</button>
  </div>`;
}

function lessonView() {
  const words = state.lessonList;
  if (!words.length) {
    return `<div class="min-h-screen pt-24 pb-28 px-4 text-center">
      <p class="mb-4 text-slate-500">В этом уроке нет слов.</p>
      <button type="button" data-go="home" class="bg-blue-600 text-white px-6 py-3 rounded-2xl font-bold">На главную</button>
    </div>`;
  }
  if (state.lessonIndex >= words.length) state.lessonIndex = words.length - 1;

  const w = words[state.lessonIndex];
  const learned = isLearned(w.id);
  const last = state.lessonIndex === words.length - 1;
  const title = state.lessonMode === "daily" ? `День ${state.progress.currentDay}` : state.lessonMode === "review" ? "Повторение" : "Все слова";

  const examples = w.examples
    .map(
      (ex) => `<div class="bg-white border border-slate-200 p-4 rounded-2xl">
        <div class="flex items-start justify-between gap-3">
          <p class="font-semibold">${esc(ex.ro)}</p>
          ${speakerBtn(ex.ro, "text-slate-300 p-1 shrink-0")}
        </div>
        <p class="text-[11px] text-slate-400 italic mt-1">[${esc(ex.transcription)}]</p>
        <p class="text-sm text-slate-500 mt-1">${esc(ex.ru)}</p>
      </div>`
    )
    .join("");

  const dots = words
    .map(
      (_, i) =>
        `<span class="h-1.5 rounded-full transition-all ${i === state.lessonIndex ? "w-5 bg-slate-900" : i < state.lessonIndex ? "w-1.5 bg-slate-400" : "w-1.5 bg-slate-200"}"></span>`
    )
    .join("");

  return `<div class="min-h-screen pt-24 pb-28 px-4 max-w-md mx-auto flex flex-col">
    <div class="flex items-center justify-between mb-1">
      <button type="button" data-go="home" class="p-2 -ml-2 text-slate-400 text-lg leading-none" aria-label="На главную">←</button>
      <div class="text-[10px] font-bold text-slate-400 uppercase">${title} • ${state.lessonIndex + 1} / ${words.length}</div>
      <span class="w-6"></span>
    </div>
    <div class="flex items-center justify-center gap-1 my-4">${dots}</div>

    <div class="text-center">
      <div class="flex items-center justify-center gap-3">
        <h2 class="text-4xl sm:text-5xl font-bold lowercase break-words">${esc(w.word)}</h2>
        ${speakerBtn(w.word, "p-3 bg-blue-100 text-blue-600 rounded-full shrink-0")}
      </div>
      <p class="text-lg text-slate-400 italic mt-2">[${esc(w.transcription)}]</p>

      <div class="mt-6">
        ${
          state.revealed
            ? `<h3 class="text-2xl font-bold text-blue-600">${esc(w.translation)}</h3>`
            : `<button type="button" data-act="reveal" class="px-8 py-4 rounded-2xl text-base font-bold border-2 border-dashed border-slate-300 text-slate-500">Показать перевод</button>`
        }
      </div>

      ${
        state.revealed
          ? `<button type="button" data-toggle="${esc(w.id)}" class="mt-4 px-6 py-2.5 rounded-2xl text-sm font-bold border-2 transition-colors ${learned ? "bg-green-500 text-white border-green-500" : "bg-white text-slate-500 border-slate-200"}">${learned ? "✓ Выучено" : "Отметить выученным"}</button>`
          : ""
      }
    </div>

    ${examples ? `<div class="mt-8 space-y-3">${examples}</div>` : ""}

    <div class="mt-auto pt-8 flex gap-3">
      <button type="button" data-act="prev-word" ${state.lessonIndex === 0 ? "disabled" : ""} class="px-5 py-4 rounded-2xl font-bold bg-white border border-slate-200 text-slate-500 disabled:opacity-40">Назад</button>
      <button type="button" data-act="next-word" class="flex-1 bg-slate-900 text-white py-4 rounded-2xl font-bold">${last ? "Завершить урок" : "Далее"}</button>
    </div>
  </div>`;
}

function dictionaryView() {
  const query = state.query.trim().toLowerCase();
  const list = state.words.filter((w) => {
    if (state.filter === "unlearned" && isLearned(w.id)) return false;
    if (!query) return true;
    return (
      w.word.toLowerCase().includes(query) ||
      w.translation.toLowerCase().includes(query) ||
      w.transcription.toLowerCase().includes(query)
    );
  });

  const rows = list
    .map((w) => {
      const learned = isLearned(w.id);
      return `<div class="bg-white p-4 rounded-2xl border border-slate-200 flex justify-between items-center gap-3 ${learned ? "opacity-60" : ""}">
        <div class="min-w-0">
          <p class="font-bold lowercase truncate">${esc(w.word)}</p>
          <p class="text-sm text-slate-500 truncate">${esc(w.translation)}</p>
        </div>
        <div class="flex gap-1 shrink-0">
          ${speakerBtn(w.word, "p-2 text-slate-300")}
          <button type="button" data-toggle="${esc(w.id)}" aria-label="${learned ? "Снять отметку" : "Отметить выученным"}" class="p-2 text-xl ${learned ? "text-green-500" : "text-slate-200"}">✓</button>
        </div>
      </div>`;
    })
    .join("");

  const tab = (value, label) =>
    `<button type="button" data-filter="${value}" class="flex-1 py-2 text-xs font-bold rounded-xl transition-colors ${state.filter === value ? "bg-white text-blue-600 shadow-sm" : "text-slate-500"}">${label}</button>`;

  return `<div class="min-h-screen pt-24 pb-28 px-4 max-w-md mx-auto">
    <div class="flex items-center gap-3 mb-4">
      <button type="button" data-go="home" class="p-2 -ml-2 text-slate-400 text-lg leading-none" aria-label="На главную">←</button>
      <h2 class="text-2xl font-black">Словарь</h2>
      <span class="ml-auto text-xs text-slate-400">${list.length} из ${state.words.length}</span>
    </div>

    <input id="search" data-search type="search" value="${esc(state.query)}" placeholder="Поиск по слову или переводу" autocomplete="off"
      class="w-full mb-3 px-4 py-3 rounded-2xl border border-slate-200 bg-white text-sm outline-none focus:border-blue-500">

    <div class="flex bg-slate-100 p-1 rounded-2xl mb-5">${tab("all", "Все")}${tab("unlearned", "Невыученные")}</div>

    <div class="space-y-3">${rows || `<p class="text-center text-slate-400 text-sm py-10">Ничего не найдено</p>`}</div>
  </div>`;
}

/* -------------------------------------------------------------------- рендер */

function render() {
  const active = document.activeElement;
  const caret = active?.id === "search" ? active.selectionStart : null;

  let main;
  if (state.status === "loading") main = loadingView();
  else if (state.status === "error") main = errorView();
  else if (state.view === "lesson") main = lessonView();
  else if (state.view === "dictionary") main = dictionaryView();
  else main = homeView();

  root.innerHTML = `<div class="min-h-screen bg-slate-50">${header()}${installBanner()}<main>${main}</main>${nav()}${toastView()}</div>`;

  if (caret !== null) {
    const input = document.getElementById("search");
    if (input) {
      input.focus();
      try {
        input.setSelectionRange(caret, caret);
      } catch (e) {
        /* не все браузеры дают управлять кареткой у type=search */
      }
    }
  }
}

/* ------------------------------------------------------------------- события */

root.addEventListener("click", async (event) => {
  const target = event.target.closest("[data-go],[data-act],[data-speak],[data-toggle],[data-filter]");
  if (!target) return;
  const { go, act, speak, toggle, filter } = target.dataset;

  if (speak !== undefined) speakRomanian(decodeURIComponent(speak));
  if (go) {
    state.view = go;
    render();
  }
  if (toggle) toggleLearned(toggle);
  if (filter) {
    state.filter = filter;
    render();
  }

  switch (act) {
    case "start-daily":
      startLesson("daily");
      break;
    case "start-review":
      startLesson("review");
      break;
    case "start-all":
      startLesson("all");
      break;
    case "quick-lesson":
      startLesson(dailyWords().some((w) => !isLearned(w.id)) ? "daily" : "review");
      break;
    case "reveal":
      state.revealed = true;
      render();
      break;
    case "prev-word":
      goToWord(state.lessonIndex - 1);
      break;
    case "next-word":
      if (state.lessonIndex >= state.lessonList.length - 1) completeLesson();
      else goToWord(state.lessonIndex + 1);
      break;
    case "next-day":
      openNextDay();
      break;
    case "reload":
      location.reload();
      break;
    case "reset":
      resetProgress();
      break;
    case "hide-install":
      state.showInstall = false;
      try {
        localStorage.setItem(INSTALL_DISMISS_KEY, "1");
      } catch (e) {
        /* ignore */
      }
      render();
      break;
    case "install":
      if (!state.deferredPrompt) break;
      state.deferredPrompt.prompt();
      await state.deferredPrompt.userChoice;
      state.deferredPrompt = null;
      state.showInstall = false;
      render();
      break;
    default:
      break;
  }
});

root.addEventListener("input", (event) => {
  if (event.target?.dataset?.search === undefined) return;
  state.query = event.target.value;
  render();
});

document.addEventListener("keydown", (event) => {
  if (state.view !== "lesson" || event.metaKey || event.ctrlKey || event.altKey) return;
  // Если фокус на управляющем элементе, им управляет браузер — не перехватываем.
  const active = document.activeElement?.tagName ?? "";
  if (/^(INPUT|TEXTAREA|BUTTON|A|SELECT)$/.test(active)) return;

  if (event.key === "ArrowRight" || event.key === " ") {
    event.preventDefault();
    if (state.lessonIndex >= state.lessonList.length - 1) completeLesson();
    else goToWord(state.lessonIndex + 1);
  } else if (event.key === "ArrowLeft") {
    event.preventDefault();
    goToWord(state.lessonIndex - 1);
  } else if (event.key.toLowerCase() === "s") {
    const word = state.lessonList[state.lessonIndex];
    if (word) speakRomanian(word.word);
  } else if (event.key === "Escape") {
    state.view = "home";
    render();
  }
});

window.addEventListener("beforeinstallprompt", (event) => {
  event.preventDefault();
  state.deferredPrompt = event;
  state.showInstall = true;
  render();
});

window.addEventListener("appinstalled", () => {
  state.deferredPrompt = null;
  state.showInstall = false;
  render();
});

/* ---------------------------------------------------------------------- init */

async function init() {
  render(); // скелетон загрузки

  const standalone = window.matchMedia("(display-mode: standalone)").matches || window.navigator.standalone === true;
  state.isiOS = /iPhone|iPad|iPod/i.test(navigator.userAgent);
  let dismissed = false;
  try {
    dismissed = localStorage.getItem(INSTALL_DISMISS_KEY) === "1";
  } catch (e) {
    /* ignore */
  }
  if (!standalone && state.isiOS && !dismissed) state.showInstall = true;

  if ("serviceWorker" in navigator) {
    navigator.serviceWorker.register("./sw.js").catch((e) => console.warn("SW не зарегистрирован", e));
  }

  try {
    state.words = await loadWords();
    normalizeProgress();
    state.status = "ready";
  } catch (error) {
    console.error(error);
    state.status = "error";
    state.error = error.message;
  }
  render();
}

init();
