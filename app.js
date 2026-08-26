const STORAGE_KEY = "romanian_daily_progress";

function speakRomanian(text) {
  if (!window.speechSynthesis) return;
  window.speechSynthesis.cancel();
  const u = new SpeechSynthesisUtterance(text);
  const voices = window.speechSynthesis.getVoices();
  const voice = voices.find((v) => v.lang.startsWith("ro")) || voices.find((v) => /romanian/i.test(v.name));
  if (voice) u.voice = voice;
  u.lang = "ro-RO";
  u.rate = 0.85;
  window.speechSynthesis.speak(u);
}

function loadProgress() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) return JSON.parse(saved);
  } catch (e) {}
  return { learnedWordIds: [], streak: 0, lastStudyDate: null, currentDay: 1 };
}

function saveProgress(p) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(p));
}

const state = {
  words: [],
  view: "home",
  lessonMode: "daily",
  lessonIndex: 0,
  progress: loadProgress(),
  filter: "all",
  deferredPrompt: null,
  showInstall: false,
  isiOS: false,
};

const root = document.getElementById("root");

function setProgress(next) {
  state.progress = next;
  saveProgress(next);
  render();
}

function speakerBtn(text, cls) {
  return `<button data-speak="${encodeURIComponent(text)}" class="${cls}" aria-label="Play">
    <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z"/></svg>
  </button>`;
}

function header() {
  return `<header class="fixed top-0 left-0 right-0 bg-white/80 backdrop-blur-md border-b border-slate-200 z-50 px-4 py-3">
    <div class="max-w-md mx-auto flex justify-between items-center">
      <div class="flex items-center gap-2" data-go="home">
        <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center text-white font-bold">R</div>
        <h1 class="font-bold text-slate-800 tracking-tight">Romanian Daily</h1>
      </div>
      <div class="flex items-center gap-1 text-orange-500 font-semibold bg-orange-50 px-3 py-1 rounded-full border border-orange-100">
        <span class="text-sm">🔥</span><span class="text-sm">${state.progress.streak}</span>
      </div>
    </div>
  </header>`;
}

function installBanner() {
  if (!state.showInstall) return "";
  const iosText = state.isiOS && !state.deferredPrompt;
  return `<div class="fixed top-16 left-4 right-4 z-[60] max-w-md mx-auto bg-slate-900 text-white p-4 rounded-2xl shadow-2xl flex items-start gap-3">
    <div class="flex-1 text-sm">
      <p class="font-bold">Установить на телефон</p>
      <p class="text-slate-300 text-xs mt-1">${iosText ? "Safari → Поделиться → На экран «Домой»" : "Нажми «Установить»"}</p>
    </div>
    ${state.deferredPrompt ? `<button data-act="install" class="bg-blue-600 text-xs font-bold px-3 py-2 rounded-xl shrink-0">Установить</button>` : ""}
    <button data-act="hide-install" class="text-slate-400 text-lg leading-none">×</button>
  </div>`;
}

function nav() {
  const home = state.view === "home" ? "text-blue-600" : "text-slate-400";
  const dict = state.view === "dictionary" ? "text-blue-600" : "text-slate-400";
  return `<nav class="fixed bottom-0 left-0 right-0 bg-white/90 backdrop-blur-lg border-t border-slate-100 px-6 py-3 flex justify-around items-center z-50">
    <button data-go="home" class="flex flex-col items-center gap-1.5 ${home}"><span class="text-[9px] font-black uppercase">Главная</span></button>
    <button data-act="start-daily" class="w-12 h-12 bg-blue-600 rounded-2xl text-white font-black -mt-10">+</button>
    <button data-go="dictionary" class="flex flex-col items-center gap-1.5 ${dict}"><span class="text-[9px] font-black uppercase">Слова</span></button>
  </nav>`;
}

function dailyWords() { return state.words.filter((w) => w.day === state.progress.currentDay); }
function reviewWords() {
  const unlearned = state.words.filter((w) => !state.progress.learnedWordIds.includes(w.id) && w.day <= state.progress.currentDay);
  return unlearned.sort(() => 0.5 - Math.random()).slice(0, 10);
}
function lessonWords() { return state.lessonMode === "daily" ? dailyWords() : reviewWords(); }

function homeView() {
  const p = state.progress;
  const current = dailyWords();
  const learnedCount = current.filter((w) => p.learnedWordIds.includes(w.id)).length;
  const finished = current.length > 0 && learnedCount >= current.length;
  const allUnlearned = state.words.filter((w) => !p.learnedWordIds.includes(w.id) && w.day <= p.currentDay);
  const pct = (learnedCount / Math.max(current.length, 1)) * 100;
  return `<div class="min-h-screen pt-20 pb-24 px-4 max-w-md mx-auto flex flex-col gap-5">
    <div class="bg-slate-900 text-white p-8 rounded-[2.5rem] shadow-2xl">
      <h2 class="text-3xl font-black">День ${p.currentDay}</h2>
      <p class="text-slate-400 text-sm mt-1">10 слов сегодня</p>
      <div class="mt-6"><div class="flex justify-between text-[11px] text-slate-400"><span>Прогресс</span><span>${learnedCount}/${current.length}</span></div>
      <div class="w-full bg-slate-800 h-2.5 rounded-full overflow-hidden mt-2"><div class="bg-blue-500 h-full" style="width:${pct}%"></div></div></div>
      ${!finished ? `<button data-act="start-daily" class="mt-6 w-full bg-blue-600 py-4 rounded-2xl font-bold">Начать урок</button>` : `<button data-act="next-day" class="mt-6 w-full bg-green-600 py-4 rounded-2xl font-bold">Открыть День ${p.currentDay + 1}</button>`}
    </div>
    <div class="grid grid-cols-2 gap-4">
      <div class="bg-white p-6 rounded-[2rem] border text-center"><div class="text-xl font-black">${p.learnedWordIds.length}</div><div class="text-[9px] text-slate-400 uppercase font-bold">Выучено</div></div>
      <div class="bg-white p-6 rounded-[2rem] border text-center"><div class="text-xl font-black">${state.words.length}</div><div class="text-[9px] text-slate-400 uppercase font-bold">Всего</div></div>
    </div>
    ${allUnlearned.length ? `<div class="bg-white border p-6 rounded-[2rem] flex items-center justify-between"><div><h3 class="font-bold">Повторение</h3><p class="text-[11px] text-slate-400">${allUnlearned.length} слов</p></div><button data-act="start-review" class="px-5 py-2.5 bg-slate-900 text-white text-xs font-bold rounded-xl">Начать</button></div>` : ""}
  </div>`;
}

function lessonView() {
  const words = lessonWords();
  if (!words.length) return `<div class="pt-24 text-center"><p class="mb-4">Урок пуст.</p><button data-go="home" class="bg-blue-600 text-white px-6 py-2 rounded-xl">Назад</button></div>`;
  if (state.lessonIndex >= words.length) state.lessonIndex = 0;
  const w = words[state.lessonIndex];
  const last = state.lessonIndex === words.length - 1;
  const learned = state.progress.learnedWordIds.includes(w.id);
  const title = state.lessonMode === "daily" ? `День ${state.progress.currentDay}` : "Повторение";
  const examples = (w.examples || []).map((ex) => `<div class="bg-white border p-4 rounded-3xl"><div class="flex justify-between"><p class="font-semibold">${ex.ro}</p>${speakerBtn(ex.ro, "text-slate-300 p-1")}</div><p class="text-[11px] text-slate-400 italic">[${ex.transcription}]</p><p class="text-sm text-slate-500">${ex.ru}</p></div>`).join("");
  return `<div class="flex flex-col min-h-screen pt-20 pb-24 px-4 max-w-md mx-auto">
    <div class="mb-6 text-[10px] font-bold text-slate-400 uppercase">${title} • ${state.lessonIndex + 1} / ${words.length}</div>
    <div class="text-center"><div class="flex items-center justify-center gap-3"><h2 class="text-5xl font-bold lowercase">${w.word}</h2>${speakerBtn(w.word, "p-3 bg-blue-100 text-blue-600 rounded-full")}</div>
    <p class="text-xl text-slate-400 italic mt-2">[${w.transcription}]</p>
    <h3 class="text-2xl font-bold text-blue-600 mt-4">${w.translation}</h3>
    <button data-toggle="${w.id}" class="mt-4 px-6 py-2.5 rounded-2xl text-sm font-bold border-2 ${learned ? "bg-green-500 text-white border-green-500" : "bg-white text-slate-400"}">${learned ? "Выучено" : "Отметить выученным"}</button></div>
    <div class="mt-8 space-y-3">${examples}</div>
    <button data-act="next-word" class="mt-8 w-full bg-slate-900 text-white py-4 rounded-2xl font-bold">${last ? "Завершить" : "Далее"}</button></div>`;
}

function dictionaryView() {
  const list = state.filter === "unlearned" ? state.words.filter((w) => !state.progress.learnedWordIds.includes(w.id)) : state.words;
  const rows = list.map((w) => {
    const learned = state.progress.learnedWordIds.includes(w.id);
    return `<div class="bg-white p-5 rounded-[1.5rem] border flex justify-between ${learned ? "opacity-60" : ""}"><div><p class="font-bold lowercase">${w.word}</p><p class="text-sm text-slate-500">${w.translation}</p></div><div class="flex gap-2">${speakerBtn(w.word, "p-2 text-slate-300")}<button data-toggle="${w.id}" class="p-2 ${learned ? "text-green-500" : "text-slate-200"}">✓</button></div></div>`;
  }).join("");
  return `<div class="min-h-screen pt-20 pb-24 px-4 max-w-md mx-auto"><div class="flex items-center gap-4 mb-6"><button data-go="home" class="p-2 bg-slate-100 rounded-xl">←</button><h2 class="text-2xl font-black">Словарь</h2></div>
  <div class="flex bg-slate-100 p-1 rounded-2xl mb-6"><button data-filter="all" class="flex-1 py-2 text-xs font-bold rounded-xl ${state.filter === "all" ? "bg-white text-blue-600" : "text-slate-500"}">Все</button><button data-filter="unlearned" class="flex-1 py-2 text-xs font-bold rounded-xl ${state.filter === "unlearned" ? "bg-white text-blue-600" : "text-slate-500"}">Невыученные</button></div>
  <div class="space-y-3">${rows || "Пусто"}</div></div>`;
}

function render() {
  let main = state.view === "home" ? homeView() : state.view === "lesson" ? lessonView() : dictionaryView();
  root.innerHTML = `<div class="min-h-screen bg-slate-50">${header()}${installBanner()}<main>${main}</main>${nav()}</div>`;
}

function toggleLearned(id) {
  const ids = state.progress.learnedWordIds.includes(id) ? state.progress.learnedWordIds.filter((x) => x !== id) : [...state.progress.learnedWordIds, id];
  setProgress({ ...state.progress, learnedWordIds: ids });
}
function completeLesson() {
  const today = new Date().toISOString().split("T")[0];
  const streak = state.progress.lastStudyDate === today ? state.progress.streak : state.progress.streak + 1;
  state.view = "home";
  setProgress({ ...state.progress, lastStudyDate: today, streak });
}

root.addEventListener("click", async (e) => {
  const t = e.target.closest("[data-go],[data-act],[data-speak],[data-toggle],[data-filter]");
  if (!t) return;
  if (t.dataset.speak) speakRomanian(decodeURIComponent(t.dataset.speak));
  if (t.dataset.go) { state.view = t.dataset.go; render(); }
  if (t.dataset.toggle) toggleLearned(t.dataset.toggle);
  if (t.dataset.filter) { state.filter = t.dataset.filter; render(); }
  if (t.dataset.act === "start-daily") { state.lessonMode = "daily"; state.lessonIndex = 0; state.view = "lesson"; render(); }
  if (t.dataset.act === "start-review") { state.lessonMode = "review"; state.lessonIndex = 0; state.view = "lesson"; render(); }
  if (t.dataset.act === "next-word") {
    const words = lessonWords();
    if (state.lessonIndex >= words.length - 1) completeLesson(); else { state.lessonIndex += 1; render(); }
  }
  if (t.dataset.act === "next-day") {
    const next = state.progress.currentDay + 1;
    if (state.words.some((w) => w.day === next)) setProgress({ ...state.progress, currentDay: next });
    else alert("Курс пройден");
  }
  if (t.dataset.act === "hide-install") { state.showInstall = false; render(); }
  if (t.dataset.act === "install" && state.deferredPrompt) {
    state.deferredPrompt.prompt();
    await state.deferredPrompt.userChoice;
    state.deferredPrompt = null; state.showInstall = false; render();
  }
});

window.addEventListener("beforeinstallprompt", (e) => { e.preventDefault(); state.deferredPrompt = e; state.showInstall = true; render(); });

(async function init() {
  const standalone = window.matchMedia("(display-mode: standalone)").matches || window.navigator.standalone;
  state.isiOS = /iPhone|iPad|iPod/i.test(navigator.userAgent);
  if (!standalone && state.isiOS) state.showInstall = true;
  if ("serviceWorker" in navigator) navigator.serviceWorker.register("./sw.js").catch(() => {});
  const packs = await Promise.all([1,2,3].map((n) => fetch("./words-part" + n + ".json").then((r) => r.json())));
  state.words = packs.flat();
  render();
})();
