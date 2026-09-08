/* SRS extension loaded after app.js */
(function () {
  const SETTINGS_KEY = "romanian_daily_settings";
  const SRS_BOXES = [1, 3, 7, 14, 30, 60];
  function todayISO() { return new Date().toISOString().split("T")[0]; }
  function addDaysISO(iso, n) {
    const d = new Date((iso || todayISO()) + "T12:00:00");
    d.setDate(d.getDate() + n);
    return d.toISOString().split("T")[0];
  }
  function loadSettings() {
    try {
      const raw = localStorage.getItem(SETTINGS_KEY);
      if (raw) {
        const s = JSON.parse(raw);
        return { voiceRate: typeof s.voiceRate === "number" ? s.voiceRate : 0.85, voiceName: s.voiceName || "", reminder: s.reminder || "" };
      }
    } catch (e) {}
    return { voiceRate: 0.85, voiceName: "", reminder: "" };
  }
  function saveSettings(s) { localStorage.setItem(SETTINGS_KEY, JSON.stringify(s)); }
  state.settings = state.settings || loadSettings();
  state.updateReady = !!state.updateReady;
  if (!state.progress.srs) state.progress.srs = {};
  if (!state.progress.hardStreak) state.progress.hardStreak = {};
  if (!state.progress.studyDays) state.progress.studyDays = [];
  window.dueSrsWords = function () {
    const today = todayISO();
    return state.words.filter(function (w) {
      const e = (state.progress.srs || {})[w.id];
      return e && e.due && e.due <= today;
    });
  };
  function applySrs(id, ok) {
    const srs = Object.assign({}, state.progress.srs || {});
    const cur = srs[id] || { box: 0, due: todayISO() };
    let box = cur.box || 0;
    if (ok) box = Math.min(SRS_BOXES.length, box + 1);
    else box = Math.max(0, box - 1);
    const interval = box === 0 ? 0 : SRS_BOXES[box - 1];
    srs[id] = { box: box, due: addDaysISO(todayISO(), interval) };
    return srs;
  }
  function noteStudyDay(p) {
    const today = todayISO();
    const days = Array.isArray(p.studyDays) ? p.studyDays.slice() : [];
    if (days.indexOf(today) < 0) days.push(today);
    return days.slice(-400);
  }
  markResult = function (id, ok) {
    const p = state.progress;
    const stats = { attempts: (p.stats.attempts || 0) + 1, correct: (p.stats.correct || 0) + (ok ? 1 : 0) };
    let wrong = p.wrongIds.filter(function (x) { return x !== id; });
    let learned = p.learnedWordIds.slice();
    const hard = Object.assign({}, p.hardStreak || {});
    if (ok) {
      if (learned.indexOf(id) < 0) learned.push(id);
      hard[id] = (hard[id] || 0) + 1;
      if (hard[id] >= 3) { wrong = wrong.filter(function (x) { return x !== id; }); delete hard[id]; }
    } else {
      if (wrong.indexOf(id) < 0) wrong.push(id);
      hard[id] = 0;
    }
    setProgress(Object.assign({}, p, { stats: stats, wrongIds: wrong, learnedWordIds: learned, srs: applySrs(id, ok), hardStreak: hard, studyDays: noteStudyDay(p) }));
  };
  reviewWords = function () {
    const ids = new Set(state.progress.wrongIds);
    const due = new Set(dueSrsWords().map(function (w) { return w.id; }));
    let pool = state.words.filter(function (w) {
      return ids.has(w.id) || due.has(w.id) || (!state.progress.learnedWordIds.includes(w.id) && w.day <= state.progress.currentDay);
    });
    if (pool.length < 4) pool = state.words.filter(function (w) { return w.day <= state.progress.currentDay; });
    return pool.sort(function () { return 0.5 - Math.random(); }).slice(0, 10);
  };
  const _home = homeView;
  homeView = function () {
    const html = _home();
    const due = dueSrsWords().length;
    const line = '<p class="due-line">' + (due ? "Сегодня к повторению: " + due : "Повторений на сегодня нет") + "</p>";
    return html.replace('<button data-act="start-day" class="btn btn-blue">Начать день</button>', line + '<button data-act="start-day" class="btn btn-blue">Начать день</button>').replace('<button data-go="import">Свои слова</button>', '<button data-go="import">Свои слова</button><button data-go="stats">Статистика</button><button data-go="settings">Настройки</button>');
  };
  window.statsView = function () {
    const p = state.progress;
    const days = new Set(p.studyDays || []);
    const cells = [];
    const start = new Date();
    start.setDate(start.getDate() - 55);
    start.setHours(12, 0, 0, 0);
    for (let i = 0; i < 56; i++) {
      const d = new Date(start);
      d.setDate(start.getDate() + i);
      const iso = d.toISOString().split("T")[0];
      cells.push('<i class="' + (days.has(iso) ? "on" : "") + '" title="' + iso + '"></i>');
    }
    return '<div class="page"><button data-go="home" class="ghost-link">← Назад</button><h2 class="h2">Статистика</h2><div class="stats3"><div class="card"><div class="n">' + p.learnedWordIds.length + '</div><div class="l">выучено</div></div><div class="card"><div class="n">' + (state.words.length || 1) + '</div><div class="l">всего</div></div><div class="card"><div class="n">' + acc() + '%</div><div class="l">точность</div></div></div><div class="block"><div class="block-h"><h3>Повторения</h3><span>' + dueSrsWords().length + ' сегодня</span></div><p class="hint">Интервалы: 1 → 3 → 7 → 14 → 30 → 60. Трудное слово уходит после 3 верных ответов.</p></div><div class="block"><div class="block-h"><h3>Календарь</h3><span>8 недель</span></div><div class="cal">' + cells.join("") + '</div><p class="hint">Серия: ' + p.streak + ' дн.</p></div></div>';
  };
  window.settingsView = function () {
    const s = state.settings || loadSettings();
    const voices = (window.speechSynthesis ? window.speechSynthesis.getVoices() : []).filter(function (v) { return v.lang; });
    const opts = ['<option value="">Авто</option>'].concat(voices.slice(0, 24).map(function (v) {
      return '<option value="' + esc(v.name) + '"' + (s.voiceName === v.name ? " selected" : "") + '>' + esc(v.name) + '</option>';
    }));
    return '<div class="page"><button data-go="home" class="ghost-link">← Назад</button><h2 class="h2">Настройки</h2><div class="block"><div class="block-h"><h3>Озвучка</h3></div><label class="hint">Скорость</label><input id="voice-rate" type="range" min="0.6" max="1.2" step="0.05" value="' + s.voiceRate + '"><select id="voice-name">' + opts.join("") + '</select></div><div class="block"><div class="block-h"><h3>Напоминание</h3></div><input id="reminder-time" type="time" value="' + esc(s.reminder || "") + '"></div><div class="block"><div class="block-h"><h3>Резервная копия</h3></div><button class="filebtn" data-act="export-backup">Скачать JSON</button><input id="backup-file" class="filebtn" type="file" accept="application/json,.json"></div></div>';
  };
  const _renderApp = renderApp;
  renderApp = function () {
    if (state.view === "stats" || state.view === "settings") {
      document.documentElement.setAttribute("data-theme", state.theme);
      const main = state.view === "stats" ? statsView() : settingsView();
      root.innerHTML = '<div class="wrap">' + header() + '<main>' + main + '</main>' + nav() + '</div>';
      return;
    }
    _renderApp();
    const page = root.querySelector(".study .word, .flip");
    if (page && !page.dataset.swipeBound) {
      page.dataset.swipeBound = "1";
      var x0 = null;
      page.addEventListener("touchstart", function (ev) { x0 = ev.changedTouches[0].clientX; }, { passive: true });
      page.addEventListener("touchend", function (ev) {
        if (x0 == null) return;
        const dx = ev.changedTouches[0].clientX - x0;
        x0 = null;
        if (Math.abs(dx) < 60) return;
        if (dx > 0) document.querySelector("[data-act=know]") && document.querySelector("[data-act=know]").click();
        else document.querySelector("[data-act=unknow]") && document.querySelector("[data-act=unknow]").click();
      }, { passive: true });
    }
  };
  function exportBackup() {
    const a = document.createElement("a");
    a.href = URL.createObjectURL(new Blob([JSON.stringify({ progress: state.progress, settings: state.settings, theme: state.theme }, null, 2)], { type: "application/json" }));
    a.download = "romanian-daily-backup.json";
    a.click();
  }
  function importBackup(file) {
    const reader = new FileReader();
    reader.onload = function () {
      try {
        const data = JSON.parse(reader.result);
        if (data.progress) setProgress(Object.assign({}, state.progress, data.progress));
        if (data.settings) { state.settings = Object.assign({}, loadSettings(), data.settings); saveSettings(state.settings); }
        if (data.theme) { state.theme = data.theme; localStorage.setItem("romanian_daily_theme", state.theme); }
        state.view = "home"; render();
      } catch (err) { alert("Не удалось прочитать файл"); }
    };
    reader.readAsText(file);
  }
  speakRomanian = function (text) {
    if (!window.speechSynthesis) return;
    window.speechSynthesis.cancel();
    const u = new SpeechSynthesisUtterance(text);
    const voices = window.speechSynthesis.getVoices();
    const pref = state.settings && state.settings.voiceName;
    const voice = (pref && voices.find(function (v) { return v.name === pref; })) || voices.find(function (v) { return v.lang && v.lang.indexOf("ro") === 0; }) || voices.find(function (v) { return /romanian/i.test(v.name); });
    if (voice) u.voice = voice;
    u.lang = "ro-RO";
    u.rate = (state.settings && state.settings.voiceRate) || 0.85;
    window.speechSynthesis.speak(u);
  };
  root.addEventListener("click", function (e) {
    const el = e.target.closest("[data-act]");
    const act = el && el.getAttribute("data-act");
    if (act === "export-backup") exportBackup();
    if (act === "apply-update") location.reload();
  });
  root.addEventListener("change", function (e) {
    if (e.target.id === "backup-file" && e.target.files && e.target.files[0]) importBackup(e.target.files[0]);
    if (e.target.id === "voice-name") { state.settings = Object.assign({}, state.settings, { voiceName: e.target.value }); saveSettings(state.settings); }
    if (e.target.id === "reminder-time") { state.settings = Object.assign({}, state.settings, { reminder: e.target.value }); saveSettings(state.settings); }
  });
  root.addEventListener("input", function (e) {
    if (e.target.id === "voice-rate") { state.settings = Object.assign({}, state.settings, { voiceRate: parseFloat(e.target.value) }); saveSettings(state.settings); }
  });
  document.addEventListener("keydown", function (e) {
    if (e.code === "Space" && ["INPUT", "TEXTAREA", "SELECT"].indexOf((e.target && e.target.tagName) || "") < 0) {
      const btn = document.querySelector("[data-act=next-word],[data-act=quiz-next],[data-act=reveal]");
      if (btn) { e.preventDefault(); btn.click(); }
    }
    if (/^[1-9]$/.test(e.key)) {
      const btn = document.querySelectorAll("[data-pick],[data-gopt]")[parseInt(e.key, 10) - 1];
      if (btn) btn.click();
    }
  });
})();
