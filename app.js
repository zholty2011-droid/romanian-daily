let WORDS=[];

const STORAGE_KEY = "romanian_daily_progress_v2";
const WORDS_KEY = "romanian_daily_custom_words";

function norm(s){
  return String(s||"").toLowerCase().trim()
    .replace(/[ăâ]/g,"a").replace(/î/g,"i").replace(/ș/g,"s").replace(/ț/g,"t")
    .replace(/[ёе]/g,"е").replace(/й/g,"и")
    .replace(/[^a-zа-я0-9]+/gi," ").replace(/\s+/g," ").trim();
}
function similar(a,b){
  a=norm(a); b=norm(b);
  if(!a||!b) return false;
  if(a===b) return true;
  if(a.includes(b)||b.includes(a)) return a.length>2 && b.length>2;
  const m=Math.max(a.length,b.length);
  if(Math.abs(a.length-b.length)>2) return false;
  let i=0,j=0,err=0;
  while(i<a.length && j<b.length){
    if(a[i]===b[j]){ i++; j++; continue; }
    err++;
    if(a[i+1]===b[j]) i++;
    else if(a[i]===b[j+1]) j++;
    else { i++; j++; }
    if(err>1) return false;
  }
  err += (a.length-i)+(b.length-j);
  return err<=1;
}
function speakRomanian(text){
  if(!window.speechSynthesis) return;
  window.speechSynthesis.cancel();
  const u=new SpeechSynthesisUtterance(text);
  const voices=window.speechSynthesis.getVoices();
  const voice=voices.find(v=>v.lang.startsWith("ro"))||voices.find(v=>/romanian/i.test(v.name));
  if(voice) u.voice=voice;
  u.lang="ro-RO"; u.rate=0.85;
  window.speechSynthesis.speak(u);
}
function loadProgress(){
  try{
    const saved=localStorage.getItem(STORAGE_KEY)||localStorage.getItem("romanian_daily_progress");
    if(saved){
      const p=JSON.parse(saved);
      return {
        learnedWordIds:p.learnedWordIds||[],
        streak:p.streak||0,
        lastStudyDate:p.lastStudyDate||null,
        currentDay:p.currentDay||1,
        wrongIds:p.wrongIds||[],
        stats:p.stats||{attempts:0,correct:0},
        exampleMarks:p.exampleMarks||{},
        seenGq:p.seenGq||[],
        quizSet:p.quizSet||{n:12,choices:6,mix:true}
      };
    }
  }catch(e){}
  return {learnedWordIds:[],streak:0,lastStudyDate:null,currentDay:1,wrongIds:[],stats:{attempts:0,correct:0},exampleMarks:{},seenGq:[],quizSet:{n:12,choices:6,mix:true}};
}
function saveProgress(p){ localStorage.setItem(STORAGE_KEY, JSON.stringify(p)); }
function loadSavedWords(){
  try{
    const raw=localStorage.getItem(WORDS_KEY);
    if(!raw) return null;
    const list=JSON.parse(raw);
    if(Array.isArray(list)&&list.length){
      const cleaned=list.map(w=>({...w, word:tidyLex(w.word), translation:tidyLex(w.translation)})).filter(isCleanWord);
      return cleaned.length?cleaned:null;
    }
  }catch(e){}
  return null;
}

let state={
  words: [],
  view:"home",
  lessonMode:"daily",
  lessonIndex:0,
  revealed:false,
  progress:loadProgress(),
  filter:"all",
  search:"",
  importMode:"replace",
  importMsg:"",
  quiz:[],
  quizIndex:0,
  quizPicked:null,
  quizDir:"ro-ru",
  typeAnswer:"",
  typeChecked:null,
  examMode:false,
  showDays:false,
  choice:null,
  phraseFilter:"all",
  gqIndex:0,
  gqPicked:null,
  gq:[],
  spellMode:false,
  quizSet:(loadProgress().quizSet)||{n:12,choices:6,mix:true},
  hintLevel:0,
  dayFlow:null,
  daySteps:["words","spell","grammar","phrases"],
  dayStep:0,
  theme: localStorage.getItem("romanian_daily_theme")||"light"
};
document.documentElement.setAttribute("data-theme", localStorage.getItem("romanian_daily_theme")||"light");
let GRAMMAR={};
let SENTENCES=[];
let GRAMMAR_Q=[];



const root=document.getElementById("root");
function setProgress(next){ state.progress=next; saveProgress(next); render(); }
function esc(s){ return String(s??"").replace(/[&<>"]/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;","\"":"&quot;"}[c])); }
function speakerBtn(text, cls){
  return `<button data-speak="${encodeURIComponent(text)}" class="${cls||"spk"}" aria-label="Слушать">
    <svg class="icon" fill="currentColor" viewBox="0 0 24 24"><path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z"/></svg>
  </button>`;
}
function header(){
  return `<header class="hdr"><div class="hdr-in">
    <div class="brand" data-go="home"><div class="logo">R</div><div class="title">Romanian Daily</div></div>
    <div class="hdr-right">
      <button class="themebtn" data-act="theme">${state.theme==="dark"?"☀️":"🌙"}</button>
      <div class="streak"><span>🔥</span><span>${state.progress.streak}</span></div>
    </div>
  </div></header>`;
}
function maxDay(){ const ds=state.words.map(w=>w.day||1); return ds.length?Math.max(1,...ds):30; }
function grammarFor(day){ return GRAMMAR[day] || ["Заметки дня","Смотри примеры в уроке."]; }

function nav(){
  const v=state.view;
  const item=(go,label,svg,on)=>`<button data-go="${go}" class="nav-btn ${on?"on":""}">${svg}<span>${label}</span></button>`;
  const homeSvg=`<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/></svg>`;
  const bookSvg=`<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/></svg>`;
  const checkSvg=`<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>`;
  return `<nav class="nav">
    ${item("home","Главная",homeSvg,v==="home")}
    <button data-act="start-daily" class="nav-plus" aria-label="Урок">
      <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/></svg>
    </button>
    ${item("practice","Проверка",checkSvg,["practice","quiz","cards","write","result"].includes(v))}
    ${item("dictionary","Слова",bookSvg,v==="dictionary")}
  </nav>`;
}
function dailyWords(){ return state.words.filter(w=>w.day===state.progress.currentDay); }
function reviewWords(){
  const ids=new Set(state.progress.wrongIds);
  let pool=state.words.filter(w=>ids.has(w.id) || (!state.progress.learnedWordIds.includes(w.id) && w.day<=state.progress.currentDay));
  if(pool.length<4) pool=state.words.filter(w=>w.day<=state.progress.currentDay);
  return pool.sort(()=>0.5-Math.random()).slice(0,10);
}
function lessonWords(){ return state.lessonMode==="daily"?dailyWords():reviewWords(); }
function acc(){
  const s=state.progress.stats;
  if(!s.attempts) return 0;
  return Math.round(100*s.correct/s.attempts);
}
function exKey(wordId, ro){ return String(wordId)+"::"+String(ro||""); }
function exampleStatus(wordId, ro){
  const marks=state.progress.exampleMarks||{};
  return marks[exKey(wordId,ro)]||null;
}
function markExample(wordId, ro, ok){
  const key=exKey(wordId,ro);
  const marks={...(state.progress.exampleMarks||{})};
  const next=ok?"know":"unknow";
  if(marks[key]===next){ render(); return; }
  marks[key]=next;
  const p=state.progress;
  setProgress({...p, exampleMarks:marks});
}
function unknownExamples(){
  const marks=state.progress.exampleMarks||{};
  const items=[];
  for(const w of state.words){
    for(const ex of (w.examples||[])){
      if(marks[exKey(w.id, ex.ro)]==="unknow") items.push({word:w, ex});
    }
  }
  return items;
}
function markResult(id, ok){
  const p=state.progress;
  const stats={attempts:(p.stats.attempts||0)+1, correct:(p.stats.correct||0)+(ok?1:0)};
  let wrong=p.wrongIds.filter(x=>x!==id);
  let learned=p.learnedWordIds.slice();
  if(ok){
    if(!learned.includes(id)) learned.push(id);
  } else if(!wrong.includes(id)) wrong.push(id);
  setProgress({...p, stats, wrongIds:wrong, learnedWordIds:learned});
}
function shuffle(arr){ return arr.map(x=>[Math.random(),x]).sort((a,b)=>a[0]-b[0]).map(x=>x[1]); }
function tidyLex(s){
  s=String(s||"").split(/[\n\r]/)[0];
  s=s.replace(/^\s*\d+[\.)]\s*/,"");
  s=s.replace(/^\s*(слово|перевод|транскрипция|транскрипція|пример|день|day)\s*[:\-–—]\s*/i,"");
  s=s.replace(/^\s*(word|translation|transcription|example)\s*[:\-–—]\s*/i,"");
  s=s.split("/")[0].trim();
  return s;
}
function looksRu(s){ return /[а-яё]/i.test(s||""); }
function looksRo(s){ return /[a-zăâîșț]/i.test(s||"") && !looksRu(s); }
function isJunkLex(s){
  s=String(s||"");
  return !s || /^(слово|перевод|транскрипция|пример)\b/i.test(s) || /https?:|undefined|null|NaN/i.test(s);
}
function isCleanWord(w){
  if(!w) return false;
  const a=tidyLex(w.word);
  const b=tidyLex(w.translation);
  if(isJunkLex(a)||isJunkLex(b)) return false;
  if(a.length<2||b.length<2||a.length>28||b.length>40) return false;
  if((a.match(/\s/g)||[]).length>2) return false;
  if(!looksRo(a) || !looksRu(b)) return false;
  return true;
}
function optionLabel(o, roToRu){
  let s=tidyLex(roToRu ? (o.translation||"") : (o.word||""));
  if(s.length>40) s=s.slice(0,38)+"…";
  return s;
}
function quizPool(){
  const custom=(state.words||[]).filter(isCleanWord);
  const base=(typeof WORDS!=="undefined"?WORDS:[]).filter(isCleanWord);
  const map=new Map();
  for(const w of base.concat(custom)){
    const k=tidyLex(w.word).toLowerCase();
    if(!map.has(k)) map.set(k,{...w, word:tidyLex(w.word), translation:tidyLex(w.translation)});
  }
  return [...map.values()];
}
function buildQuiz(pool, n){
  const clean=quizPool();
  const src=shuffle((pool||[]).filter(isCleanWord)).slice(0,n);
  const base=src.length?src:shuffle(clean).slice(0,n);
  return base.map(w=>{
    const roToRu=state.quizDir==="ro-ru";
    const target=roToRu?String(w.translation||""):String(w.word||"");
    let candidates=clean.filter(x=>x.id!==w.id && x.day===w.day);
    if(candidates.length<3) candidates=clean.filter(x=>x.id!==w.id && Math.abs((x.day||0)-(w.day||0))<=3);
    if(candidates.length<3) candidates=clean.filter(x=>x.id!==w.id);
    candidates=shuffle(candidates).sort((a,b)=>{
      const la=(roToRu?String(a.translation||""):String(a.word||"")).length;
      const lb=(roToRu?String(b.translation||""):String(b.word||"")).length;
      return Math.abs(la-target.length)-Math.abs(lb-target.length);
    });
    // unique labels so options don't look identical
    const others=[];
    const seen=new Set([optionLabel(w, roToRu).toLowerCase()]);
    for(const c of candidates){
      const lab=optionLabel(c, roToRu).toLowerCase();
      if(seen.has(lab)) continue;
      seen.add(lab);
      others.push(c);
      if(others.length>=((state.quizSet&&state.quizSet.choices)||4)-1) break;
    }
    const need=((state.quizSet&&state.quizSet.choices)||4)-1;
    while(others.length<need && clean.length>others.length+1){
      const c=clean[Math.floor(Math.random()*clean.length)];
      if(!c || c.id===w.id) continue;
      const lab=optionLabel(c, roToRu).toLowerCase();
      if(seen.has(lab)) continue;
      seen.add(lab); others.push(c);
    }
    return {word:w, options:shuffle([w,...others])};
  });
}
function startQuiz(kind){
  const pool=kind==="wrong"
    ? state.words.filter(w=>state.progress.wrongIds.includes(w.id))
    : kind==="day" ? dailyWords()
    : state.words.filter(w=>w.day<=state.progress.currentDay);
  let list=(pool||[]).filter(isCleanWord);
  if(!list.length) list=state.words.filter(isCleanWord);
  const n=(state.quizSet&&state.quizSet.n)||12;
  if(state.quizSet&&state.quizSet.mix) state.quizDir=Math.random()<0.5?"ro-ru":"ru-ro";
  state.quiz=buildQuiz(list, Math.min(n, Math.max(list.length,1)));
  state.quizIndex=0; state.quizPicked=null;
  state.view="quiz";
  render();
}
function startCards(kind){
  const pool=kind==="day"?dailyWords():reviewWords();
  state.quiz=shuffle(pool).map(w=>({word:w}));
  state.quizIndex=0; state.revealed=false;
  state.view="cards";
  render();
}
function startWrite(kind){
  const pool=kind==="day"?dailyWords():reviewWords();
  state.quiz=shuffle(pool).map(w=>({word:w}));
  state.quizIndex=0; state.typeAnswer=""; state.typeChecked=null; state.hintLevel=0;
  state.view="write";
  render();
}

function dayPhrases(){
  const all=allPhrases();
  if(!all.length) return [];
  const d=state.progress.currentDay||1;
  const out=[];
  for(let i=0;i<3;i++) out.push(all[((d-1)*3+i)%all.length]);
  const pub=all.filter(x=>x.place==="out");
  if(pub.length) out.push(pub[(d-1)%pub.length]);
  const seen=new Set();
  return out.filter(s=>{const k=s.id||s.ro; if(seen.has(k)) return false; seen.add(k); return true;});
}
function shuffleQ(q){
  const pairs=q.opts.map((o,i)=>({o,ok:i===q.a}));
  const sh=shuffle(pairs);
  return {...q, opts:sh.map(x=>x.o), a:sh.findIndex(x=>x.ok)};
}
function markSeenGq(ids){
  const prev=state.progress.seenGq||[];
  const next=[...new Set(prev.concat(ids))].slice(-80);
  state.progress={...state.progress, seenGq:next};
  saveProgress(state.progress);
}
function pickGrammar(n){
  const seen=new Set(state.progress.seenGq||[]);
  const fresh=shuffle(GRAMMAR_Q.filter(q=>!seen.has(q.id)));
  const rest=shuffle(GRAMMAR_Q.filter(q=>seen.has(q.id)));
  const out=fresh.concat(rest).slice(0,n).map(shuffleQ);
  markSeenGq(out.map(q=>q.id).filter(Boolean));
  return out;
}
function wordToGrammarItems(n){
  const pool=quizPool();
  const today=shuffle(dailyWords().map(w=>({...w,word:tidyLex(w.word),translation:tidyLex(w.translation)})).filter(isCleanWord));
  const src=today.length?today:shuffle(pool);
  const items=[];
  for(const w of src){
    if(items.length>=n) break;
    const others=shuffle(pool.filter(x=>tidyLex(x.word).toLowerCase()!==w.word.toLowerCase()));
    const roOpts=shuffle([w.word, ...others.filter(x=>looksRo(x.word)).slice(0,5).map(x=>x.word)]);
    const ruOpts=shuffle([w.translation, ...others.filter(x=>looksRu(x.translation)).slice(0,5).map(x=>x.translation)]);
    const ro= [...new Set(roOpts.map(tidyLex))].filter(s=>looksRo(s)&&!isJunkLex(s)).slice(0,6);
    const ru= [...new Set(ruOpts.map(tidyLex))].filter(s=>looksRu(s)&&!isJunkLex(s)).slice(0,6);
    if(ro.length<4 || ru.length<4) continue;
    if(!ro.includes(w.word)) ro[0]=w.word;
    if(!ru.includes(w.translation)) ru[0]=w.translation;
    if(Math.random()<0.5){
      items.push(shuffleQ({id:"w-"+w.id+"-ro",tag:"lex",q:"Как сказать: «"+w.translation+"»?",opts:ro,a:Math.max(0,ro.indexOf(w.word)),why:w.word+" — "+w.translation}));
    } else {
      items.push(shuffleQ({id:"w-"+w.id+"-ru",tag:"lex",q:"Что значит «"+w.word+"»?",opts:ru,a:Math.max(0,ru.indexOf(w.translation)),why:w.word+" — "+w.translation}));
    }
  }
  return items;
}
function dayGrammarQs(){
  const set=state.quizSet||{n:12,choices:6};
  const g=pickGrammar(Math.max(4, Math.ceil((set.n||12)/2)));
  const w=wordToGrammarItems(Math.max(4, Math.floor((set.n||12)/2)));
  return shuffle(g.concat(w)).slice(0,set.n||12);
}
function homeView(){
  const p=state.progress;
  const current=dailyWords();
  const learnedCount=current.filter(w=>p.learnedWordIds.includes(w.id)).length;
  const pct=(learnedCount/Math.max(current.length,1))*100;
  return `<div class="page home">
    <div class="hero">
      <p class="kicker">Romanian Daily</p>
      <h2>День ${p.currentDay}</h2>
      <p>Слова → написание → грамматика → фразы</p>
      <div class="bar-label"><span>${learnedCount} из ${current.length||10}</span><span>${p.learnedWordIds.length} всего</span></div>
      <div class="bar"><i style="width:${pct}%"></i></div>
      <button data-act="start-day" class="btn btn-blue">Начать день</button>
    </div>
    ${state.showDays?`<div class="days">${[...new Set(state.words.map(w=>Number(w.day)||1))].sort((a,b)=>a-b).map(d=>`<button data-day="${d}" class="daybtn ${d===p.currentDay?"on":""}">${d}</button>`).join("")}</div>`:""}
    <div class="actions">
      <button data-act="toggle-days">Выбрать день</button>
      <button data-go="practice">Проверка</button>
      <button data-go="phrases">Все фразы</button>
      <button data-go="dictionary">Словарь</button>
      <button data-go="import">Свои слова</button>
    </div>
  </div>`;
}

function importView(){
  return `<div class="page">
    <button data-go="home" class="ghost-link">← Назад</button>
    <h2 class="h2">Свои слова</h2>
    <p class="hint">Excel, Word, CSV или текст: bună — привет</p>
    <div class="modes">
      <label><input type="radio" name="imode" data-mode="append" ${state.importMode==="append"?"checked":""}> Добавить</label>
      <label><input type="radio" name="imode" data-mode="replace" ${state.importMode==="replace"?"checked":""}> Заменить</label>
    </div>
    <input id="file-input" class="filebtn" type="file" accept=".xlsx,.csv,.docx,.txt,.tsv">
    <textarea id="paste-words" rows="4" placeholder="bună — привет [бу-н]"></textarea>
    <button class="filebtn" data-act="import-paste">Загрузить из текста</button>
    <div class="links"><button data-act="reset-words">Вернуть курс</button></div>
    ${state.importMsg?`<div class="status ${String(state.importMsg).startsWith("Ошибка")?"err":""}">${esc(state.importMsg)}</div>`:""}
  </div>`;
}

function dock(inner){
  return `<div class="dock">${inner}</div>`;
}
function choiceBtns(){
  return `<div class="row2">
      <button data-act="unknow" class="pill bad ${state.choice==="unknow"?"on":""}">Не знаю</button>
      <button data-act="know" class="pill good ${state.choice==="know"?"on":""}">Знаю</button>
    </div>`;
}
function lessonView(){
  const words=lessonWords();
  if(!words.length) return `<div class="empty"><p>Урок пуст.</p><button data-go="home" class="btn btn-blue">Назад</button></div>`;
  if(state.lessonIndex>=words.length) state.lessonIndex=0;
  const w=words[state.lessonIndex];
  const last=state.lessonIndex===words.length-1;
  const title=state.lessonMode==="daily"?`День ${state.progress.currentDay}`:"Повтор";
  const examples=(w.examples||[]).map((ex,i)=>{
    const st=exampleStatus(w.id, ex.ro);
    return `<div class="ex ${st?("ex-"+st):""}">
      <div class="ex-top"><p class="ro">${esc(ex.ro)}</p>${speakerBtn(ex.ro,"ghost")}</div>
      <p class="tr">[${esc(ex.transcription||w.transcription||"")}]</p>
      ${state.revealed?`<p class="ru">${esc(ex.ru||"")}</p>`:""}
      <div class="row2">
        <button data-ex="${i}" data-ex-act="unknow" class="pill bad ${st==="unknow"?"on":""}">Не знаю</button>
        <button data-ex="${i}" data-ex-act="know" class="pill good ${st==="know"?"on":""}">Знаю</button>
      </div>
    </div>`;
  }).join("");
  return `<div class="page study">
    <div class="lesson-top">
      <div class="muted"><span>${title} • ${state.lessonIndex+1}/${words.length}</span><span>${state.dayFlow?"шаг "+(state.dayStep+1)+"/"+state.daySteps.length:"Слово"}</span></div>
      <div class="bar light"><i style="width:${((state.lessonIndex+1)/words.length)*100}%"></i></div>
    </div>
    <div class="word">
      <div class="row">
        <h2>${esc(w.word)}</h2>
        ${speakerBtn(w.word,"spk")}
      </div>
      <p class="tr">[${esc(w.transcription||"")}]</p>
      ${state.revealed?`<h3 class="ru">${esc(w.translation)}</h3>`:""}
    </div>
    ${examples?`<div><div class="ex-title">Примеры</div>${examples}</div>`:""}
    ${dock(state.revealed
      ? `${choiceBtns()}${state.choice?`<button data-act="next-word" class="btn btn-dark">${last?"Завершить":"Далее"}</button>`:""}`
      : `<button data-act="reveal" class="btn btn-blue">Показать перевод</button>`)}
  </div>`;
}

function practiceView(){
  const wrong=state.progress.wrongIds.length;
  return `<div class="page">
    <h2 class="h2">Проверка</h2>
    <p class="hint">Закрепи слова тестом, карточками или письмом.</p>
    <div class="dir">
      <button data-dir="ro-ru" class="${state.quizDir==="ro-ru"?"on":""}">RO → RU</button>
      <button data-dir="ru-ro" class="${state.quizDir==="ru-ro"?"on":""}">RU → RO</button>
    </div>
    <div class="dir">
      <button data-act="qs-n-8" class="${(state.quizSet.n===8)?"on":""}">8 вопр.</button>
      <button data-act="qs-n-12" class="${(state.quizSet.n===12)?"on":""}">12 вопр.</button>
      <button data-act="qs-n-20" class="${(state.quizSet.n===20)?"on":""}">20 вопр.</button>
    </div>
    <div class="dir">
      <button data-act="qs-c-4" class="${(state.quizSet.choices===4)?"on":""}">4 варианта</button>
      <button data-act="qs-c-6" class="${(state.quizSet.choices===6)?"on":""}">6 вариантов</button>
      <button data-act="qs-mix" class="${state.quizSet.mix?"on":""}">мешать направление</button>
    </div>
    <button data-act="quiz-day" class="menu">Тест дня — 4 варианта</button>
    <button data-act="quiz-all" class="menu">Тест по открытым дням</button>
    <button data-act="examples-day" class="menu">Примеры дня</button>
    <button data-act="examples-wrong" class="menu">Примеры, которые не помню</button>
    <button data-act="cards-day" class="menu">Карточки дня</button>
    <button data-act="spell-day" class="menu">Написание — введи слово по-румынски</button>
    <button data-act="write-day" class="menu">Написать перевод</button>
    <button data-act="gquiz" class="menu">Тест по грамматике</button>
    <button data-go="phrases" class="menu">100 фраз на каждый день</button>
    <button data-act="quiz-wrong" class="menu">Разобрать ошибки (${wrong})</button>
    <button data-act="exam" class="menu">Экзамен — 20 слов без подсказок</button>
    <button data-go="grammar" class="menu">Грамматика по дням</button>
  </div>`;
}

function quizView(){
  if(!state.quiz.length) return practiceView();
  if(state.quizIndex>=state.quiz.length){
    const ok=state.quiz.filter(q=>q.ok).length;
    const pctN=Math.round(100*ok/Math.max(state.quiz.length,1));
    const grade=pctN>=90?"A":pctN>=75?"B":pctN>=60?"C":pctN>=40?"D":"F";
    return `<div class="page center">
      <h2 class="h2">${state.examMode?"Экзамен":"Результат"}</h2>
      <div class="grade">${grade}</div>
      <div class="score">${ok}/${state.quiz.length} · ${pctN}%</div>
      <p class="hint">${pctN>=90?"Отлично.":pctN>=70?"Хорошо, разбери ошибки.":"Вернись к карточкам."}</p>
      <button data-act="retry-wrong" class="btn btn-blue">Только ошибки</button>
      <button data-go="practice" class="btn btn-dark">К проверке</button>
    </div>`;
  }
  const q=state.quiz[state.quizIndex];
  const w=q.word;
  const roToRu=state.quizDir==="ro-ru";
  const prompt=roToRu?w.word:w.translation;
  const sub=state.examMode?"":(roToRu?`[${w.transcription||""}]`:"");
  const label=o=>optionLabel(o, roToRu);
  const opts=q.options.map(o=>{
    let cls="opt";
    if(state.quizPicked){
      if(o.id===w.id) cls+=" right";
      else if(o.id===state.quizPicked) cls+=" wrong";
    }
    return `<button data-pick="${o.id}" class="${cls}">${esc(label(o))}</button>`;
  }).join("");
  return `<div class="page">
    <div class="muted"><span>Вопрос ${state.quizIndex+1}/${state.quiz.length}</span><span>${roToRu?"RO → RU":"RU → RO"}</span></div>
    <div class="bar light"><i style="width:${((state.quizIndex)/state.quiz.length)*100}%"></i></div>
    <div class="word">
      <div class="row"><h2 class="q">${esc(prompt)}</h2>${(!state.examMode && roToRu)?speakerBtn(w.word,"spk"):""}</div>
      <p class="tr">${esc(sub)}</p>
    </div>
    <div class="opts">${opts}</div>
    ${state.quizPicked?`<button data-act="quiz-next" class="btn btn-dark">Далее</button>`:""}
  </div>`;
}

function cardsView(){
  if(!state.quiz.length) return practiceView();
  if(state.quizIndex>=state.quiz.length){
    return `<div class="page center"><h2 class="h2">Карточки пройдены</h2>
      <button data-go="practice" class="btn btn-blue">К проверке</button></div>`;
  }
  const w=state.quiz[state.quizIndex].word;
  return `<div class="page study">
    <div class="muted"><span>Карточка ${state.quizIndex+1}/${state.quiz.length}</span></div>
    <div class="bar light"><i style="width:${((state.quizIndex+1)/state.quiz.length)*100}%"></i></div>
    <div class="flip">
      <div class="flip-ro">${esc(w.word)} ${speakerBtn(w.word,"spk")}</div>
      ${state.revealed?`<div class="flip-ru">${esc(w.translation)}<div class="tr">[${esc(w.transcription||"")}]</div></div>`:`<div class="ghost-hint">Сначала вспомни перевод</div>`}
    </div>
    ${dock(state.revealed
      ? `${choiceBtns()}${state.choice?`<button data-act="next-card" class="btn btn-dark">${state.quizIndex>=state.quiz.length-1?"Готово":"Далее"}</button>`:""}`
      : `<button data-act="reveal" class="btn btn-blue">Показать перевод</button>`)}
  </div>`;
}
function startExamples(onlyUnknown){
  let items=[];
  if(onlyUnknown) items=unknownExamples();
  else {
    const pool=dailyWords().length?dailyWords():state.words.filter(w=>w.day===state.progress.currentDay);
    for(const w of pool){
      for(const ex of (w.examples||[])) items.push({word:w, ex});
    }
  }
  if(!items.length){ alert(onlyUnknown?"Нет примеров с отметкой «не помню»":"У этого дня нет примеров"); return; }
  state.quiz=items; state.quizIndex=0; state.revealed=false; state.choice=null;
  state.view="examples"; render();
}
function examplesView(){
  if(!state.quiz.length) return practiceView();
  if(state.quizIndex>=state.quiz.length){
    const marks=state.progress.exampleMarks||{};
    const knowN=Object.values(marks).filter(v=>v==="know").length;
    const badN=Object.values(marks).filter(v=>v==="unknow").length;
    return `<div class="page center"><h2 class="h2">Примеры пройдены</h2>
      <p class="hint">Знаю: ${knowN} · Не помню: ${badN}</p>
      <button data-act="examples-wrong" class="btn btn-blue">Только те, что не помню</button>
      <button data-go="practice" class="btn btn-dark">К проверке</button></div>`;
  }
  const item=state.quiz[state.quizIndex];
  const w=item.word, ex=item.ex;
  return `<div class="page study">
    <div class="muted"><span>Пример ${state.quizIndex+1}/${state.quiz.length}</span><span>${esc(w.word)}</span></div>
    ${(()=>{const st=exampleStatus(w.id, ex.ro); return st?`<div class="ex-mark ${st}">${st==="know"?"Уже отмечал: знаю":"Уже отмечал: не помню"}</div>`:"";})()}
    <div class="bar light"><i style="width:${((state.quizIndex+1)/state.quiz.length)*100}%"></i></div>
    <div class="flip">
      <div class="flip-ro" style="font-size:26px">${esc(ex.ro)} ${speakerBtn(ex.ro,"spk")}</div>
      <p class="tr">[${esc(ex.transcription||w.transcription||"")}]</p>
      ${state.revealed
        ? `<div class="flip-ru">${esc(ex.ru||"")}<div class="tr">${esc(w.word)} — ${esc(w.translation)}</div></div>`
        : `<div class="ghost-hint">Что это значит?</div>`}
    </div>
    ${dock(state.revealed
      ? `${choiceBtns()}${state.choice?`<button data-act="next-ex" class="btn btn-dark">${state.quizIndex>=state.quiz.length-1?"Готово":"Далее"}</button>`:""}`
      : `<button data-act="reveal" class="btn btn-blue">Показать перевод</button>`)}
  </div>`;
}

function targetWord(w){
  if(!w) return "";
  return (state.spellMode || state.quizDir!=="ro-ru") ? w.word : w.translation;
}
function assocHint(w){
  const ro=(w.word||"").toLowerCase();
  const ru=(w.translation||"").split("/")[0].trim();
  const ex=((w.examples||[])[0]||{}).ro||"";
  const bits=[];
  if(ru) bits.push("смысл: "+ru);
  if(w.transcription) bits.push("звук как «"+w.transcription+"»");
  if(ex) bits.push("фраза: "+ex);
  if(/ă|â|î|ș|ț/i.test(ro)) bits.push("есть особая буква ă/â/î/ș/ț");
  return bits[0]||"вспомни звучание";
}
function letterBankFor(text){
  const extra="aeiorunltcmăâîșț";
  const base=[...String(text||"").toLowerCase()].filter(ch=>ch.trim());
  const pool=base.slice();
  while(pool.length<Math.max(8, base.length+3)){
    pool.push(extra[Math.floor(Math.random()*extra.length)]);
  }
  return shuffle(pool);
}
function writeHints(expect, w){
  const lvl=state.hintLevel||0;
  const letters=[...String(expect||"")];
  const mask=letters.map((ch,i)=>ch===" "?" ":((lvl>=1 && i===0)||lvl>=2?ch:"·")).join(" ");
  return `<div class="hints">
      <div class="mask">${esc(mask)} <span class="len">${letters.filter(c=>c.trim()).length} букв</span></div>
      ${lvl>=1?`<p class="assoc">${esc(assocHint(w))}</p>`:""}
      ${lvl>=2?`<p class="assoc">начало: ${esc((expect||"").slice(0,2))}…</p>`:""}
      ${state.typeChecked===null?`<button data-act="more-hint" class="ghost-link">${lvl>=2?"Подсказки открыты":"Ещё подсказка"}</button>`:""}
    </div>`;
}
function letterPad(expect){
  const q=state.quiz[state.quizIndex];
  if(q && !q.bank) q.bank=letterBankFor(expect);
  const bank=(q&&q.bank||[]).map(ch=>`<button type="button" data-act="dia" data-ch="${esc(ch)}" class="tilech">${esc(ch)}</button>`).join("");
  const dia=["ă","â","î","ș","ț"].map(ch=>`<button type="button" data-act="dia" data-ch="${ch}">${ch}</button>`).join("");
  return `<div class="letter-pad">
      <div class="typed">${esc(state.typeAnswer)||'<span class="ghost">слово появится здесь</span>'}</div>
      <div class="bank">${bank}</div>
      <div class="diac">${dia}<button type="button" data-act="backspace" class="tilech del">⌫</button></div>
    </div>`;
}
function writeView(){
  if(!state.quiz.length) return practiceView();
  if(state.quizIndex>=state.quiz.length){
    const ok=state.quiz.filter(q=>q.ok).length;
    return `<div class="page center"><h2 class="h2">Написание</h2><div class="score">${ok}/${state.quiz.length}</div>
      ${state.dayFlow?flowNextBtn("Дальше"):`<button data-go="practice" class="btn btn-blue">К проверке</button>`}</div>`;
  }
  const w=state.quiz[state.quizIndex].word;
  const roToRu=state.quizDir==="ro-ru" && !state.spellMode;
  const ask=roToRu?w.word:w.translation;
  const expect=targetWord(w);
  let fb="";
  if(state.typeChecked===true) fb=`<div class="okmsg">Верно</div>`;
  if(state.typeChecked===false) fb=`<div class="nomsg">Правильно: <b>${esc(expect)}</b></div>`;
  return `<div class="page">
    <div class="muted"><span>${state.spellMode?"Написание":"Письмо"} ${state.quizIndex+1}/${state.quiz.length}</span><span>${roToRu?"перевод":"румынское слово"}</span></div>
    <div class="word">
      <div class="row"><h2 class="q">${esc(ask)}</h2>${speakerBtn(w.word,"spk")}</div>
    </div>
    ${writeHints(expect,w)}
    <input id="type-box" class="typebox" value="${esc(state.typeAnswer)}" placeholder="cuvânt în română" ${state.typeChecked!==null?"disabled":""} autocomplete="off" autocorrect="off" spellcheck="false">
    ${letterPad(expect)}
    ${fb}
    ${state.typeChecked===null
      ? `<button data-act="check-type" class="btn btn-blue">Проверить</button>`
      : `<button data-act="write-next" class="btn btn-dark">Далее</button>`}
  </div>`;
}

function dictionaryView(){
  const q=norm(state.search);
  let list=state.words;
  if(state.filter==="unlearned") list=list.filter(w=>!state.progress.learnedWordIds.includes(w.id));
  if(state.filter==="wrong") list=list.filter(w=>state.progress.wrongIds.includes(w.id));
  if(q) list=list.filter(w=>norm(w.word).includes(q)||norm(w.translation).includes(q));
  const rows=list.slice(0,200).map(w=>{
    const learned=state.progress.learnedWordIds.includes(w.id);
    const weak=state.progress.wrongIds.includes(w.id);
    return `<div class="drow ${learned?"dim":""}">
      <div><p class="w">${esc(w.word)} ${weak?"⚠️":""}</p><p class="t">${esc(w.translation)}</p></div>
      <div class="row">
        ${speakerBtn(w.word,"ghost")}
        <button data-toggle="${w.id}" class="ok ${learned?"on":""}">✓</button>
      </div>
    </div>`;
  }).join("");
  return `<div class="page">
    <h2 class="h2">Словарь</h2>
    <input id="search-box" class="typebox" value="${esc(state.search)}" placeholder="Поиск: bună или привет">
    <div class="tabs">
      <button data-filter="all" class="${state.filter==="all"?"on":""}">Все</button>
      <button data-filter="unlearned" class="${state.filter==="unlearned"?"on":""}">Новые</button>
      <button data-filter="wrong" class="${state.filter==="wrong"?"on":""}">Ошибки</button>
    </div>
    <div>${rows||'<p class="empty">Ничего не найдено</p>'}</div>
  </div>`;
}

function grammarView(){
  const day=state.progress.currentDay;
  const items=Object.keys(GRAMMAR).map(Number).sort((a,b)=>a-b).map(d=>{
    const g=GRAMMAR[d];
    return `<div class="gbox"><h3>День ${d}: ${esc(g[0])}</h3><ul>${g.slice(1).map(x=>`<li>${esc(x)}</li>`).join("")}</ul></div>`;
  }).join("");
  return `<div class="page"><button data-go="home" class="ghost-link">← Назад</button><h2 class="h2">Грамматика</h2><button data-act="gquiz" class="btn btn-blue">Пройти тест</button>${items}</div>`;
}
function startExam(){
  const pool=state.words.filter(w=>w.day<=state.progress.currentDay);
  const list=pool.length>=8?pool:state.words;
  state.examMode=true;
  state.quiz=buildQuiz(list, Math.min(20, list.length));
  state.quizIndex=0; state.quizPicked=null; state.view="quiz";
  render();
}
function allPhrases(){
  let extra=[];
  try{ extra=JSON.parse(localStorage.getItem("romanian_daily_phrases")||"[]"); }catch(e){}
  return SENTENCES.concat(Array.isArray(extra)?extra:[]);
}
function phrasesView(){
  const f=state.phraseFilter;
  let list=allPhrases();
  if(f==="home") list=list.filter(x=>x.place==="home");
  if(f==="out") list=list.filter(x=>x.place==="out");
  const rows=list.map(s=>`<div class="ex">
      <div class="ex-top"><p class="ro">${esc(s.ro)}</p>${speakerBtn(s.ro,"ghost")}</div>
      <p class="ru">${esc(s.ru)}</p>
      <p class="tr">${s.place==="home"?"дом":s.place==="out"?"на улице / с людьми":"своё"}</p>
    </div>`).join("");
  return `<div class="page">
    <button data-go="home" class="ghost-link">← Назад</button>
    <h2 class="h2">Предложения</h2>
    <p class="hint">${list.length} фраз для дома и общения.</p>
    <div class="tabs">
      <button data-act="pf-all" class="${f==="all"?"on":""}">Все</button>
      <button data-act="pf-home" class="${f==="home"?"on":""}">Дом</button>
      <button data-act="pf-out" class="${f==="out"?"on":""}">На людях</button>
    </div>
    <button data-act="phrase-study" class="btn btn-blue">Учить фразы</button>
    <button data-act="phrase-write" class="btn btn-dark">Написать фразу</button>
    <textarea id="paste-phrases" rows="3" placeholder="Adaugă: Mulțumesc — Спасибо"></textarea>
    <button class="filebtn" data-act="import-phrases">Добавить свои предложения</button>
    ${rows}
  </div>`;
}
function phrasePool(){
  let list=allPhrases();
  if(state.phraseFilter==="home") list=list.filter(x=>x.place==="home");
  if(state.phraseFilter==="out") list=list.filter(x=>x.place==="out");
  return list;
}
function startPhraseStudy(){
  state.quiz=shuffle(phrasePool()).map(p=>({phrase:p}));
  state.quizIndex=0; state.revealed=false; state.choice=null;
  state.view="phrase"; render();
}
function startPhraseWrite(){
  state.quiz=shuffle(phrasePool()).slice(0,20).map(p=>({phrase:p,ok:null}));
  state.quizIndex=0; state.typeAnswer=""; state.typeChecked=null;
  state.view="pwrite"; render();
}
function phraseStudyView(){
  if(!state.quiz.length) return phrasesView();
  if(state.quizIndex>=state.quiz.length){
    return `<div class="page center"><h2 class="h2">Фразы</h2>
      ${state.dayFlow?flowNextBtn("Закончить день"):`<button data-go="phrases" class="btn btn-blue">Назад</button>`}</div>`;
  }
  const p=state.quiz[state.quizIndex].phrase;
  return `<div class="page study">
    <div class="muted"><span>Фраза ${state.quizIndex+1}/${state.quiz.length}</span><span>${p.place==="home"?"дом":"на людях"}</span></div>
    <div class="bar light"><i style="width:${((state.quizIndex+1)/state.quiz.length)*100}%"></i></div>
    <div class="flip">
      <div class="flip-ro" style="font-size:26px">${esc(p.ro)} ${speakerBtn(p.ro,"spk")}</div>
      ${state.revealed?`<div class="flip-ru">${esc(p.ru)}</div>`:`<div class="ghost-hint">Что это значит?</div>`}
    </div>
    ${dock(state.revealed
      ? `${choiceBtns()}<button data-act="phrase-next" class="btn btn-dark">${state.quizIndex>=state.quiz.length-1?"Готово":"Далее"}</button>`
      : `<button data-act="reveal" class="btn btn-blue">Показать перевод</button>`)}
  </div>`;
}
function phraseWriteView(){
  if(!state.quiz.length) return phrasesView();
  if(state.quizIndex>=state.quiz.length){
    const ok=state.quiz.filter(q=>q.ok).length;
    return `<div class="page center"><h2 class="h2">Написание фраз</h2><div class="score">${ok}/${state.quiz.length}</div>
      <button data-go="phrases" class="btn btn-blue">Назад</button></div>`;
  }
  const p=state.quiz[state.quizIndex].phrase;
  let fb="";
  if(state.typeChecked===true) fb=`<div class="okmsg">Верно</div>`;
  if(state.typeChecked===false) fb=`<div class="nomsg">Правильно: <b>${esc(p.ro)}</b></div>`;
  return `<div class="page">
    <div class="muted"><span>Напиши ${state.quizIndex+1}/${state.quiz.length}</span><span>по-румынски</span></div>
    <div class="word"><h2 class="q" style="font-size:28px">${esc(p.ru)}</h2></div>
    <p class="assoc">начало: ${esc((p.ro||"").split(" ")[0])}</p>
    <input id="type-box" class="typebox" value="${esc(state.typeAnswer)}" placeholder="scrie în română" ${state.typeChecked!==null?"disabled":""} autocomplete="off">
    <div class="letter-pad">
      <div class="typed">${esc(state.typeAnswer)||'<span class="ghost">фраза появится здесь</span>'}</div>
      <div class="diac">${["ă","â","î","ș","ț"].map(ch=>`<button type="button" data-act="dia" data-ch="${ch}">${ch}</button>`).join("")}<button type="button" data-act="backspace" class="tilech del">⌫</button></div>
    </div>
    ${fb}
    ${state.typeChecked===null
      ? `<button data-act="check-phrase" class="btn btn-blue">Проверить</button>`
      : `<button data-act="pwrite-next" class="btn btn-dark">Далее</button>`}
  </div>`;
}
function startGrammarQuiz(){
  state.gq=dayGrammarQs();
  state.gqIndex=0; state.gqPicked=null; state.view="gquiz"; render();
}
function gquizView(){
  if(state.gqIndex>=state.gq.length){
    const ok=state.gq.filter(q=>q.ok).length;
    const pct=Math.round(100*ok/Math.max(state.gq.length,1));
    return `<div class="page center"><h2 class="h2">Грамматика</h2>
      <div class="score">${ok}/${state.gq.length} · ${pct}%</div>
      ${state.dayFlow?flowNextBtn("Дальше"):`<button data-act="gquiz" class="btn btn-blue">Ещё раз</button><button data-go="grammar" class="btn btn-dark">К правилам</button>`}</div>`;
  }
  const q=state.gq[state.gqIndex];
  const opts=q.opts.map((o,i)=>{
    let cls="opt";
    if(state.gqPicked!==null){
      if(i===q.a) cls+=" right";
      else if(i===state.gqPicked) cls+=" wrong";
    }
    return `<button data-gopt="${i}" class="${cls}" ${state.gqPicked!==null?"disabled":""}>${esc(o)}</button>`;
  }).join("");
  return `<div class="page">
    <div class="muted"><span>Вопрос ${state.gqIndex+1}/${state.gq.length}</span></div>
    <div class="bar light"><i style="width:${((state.gqIndex+1)/state.gq.length)*100}%"></i></div>
    <h2 class="h2" style="font-size:22px">${esc(q.q)}</h2>
    <div class="opts">${opts}</div>
    ${state.gqPicked!==null?`<p class="hint">${esc(q.why||"")}</p><button data-act="gquiz-next" class="btn btn-dark">Далее</button>`:""}
  </div>`;
}
function renderApp(){
  if(state.loadError){ root.innerHTML=errorView(); return; }
  let main="";
  if(state.view==="home") main=homeView();
  else if(state.view==="lesson") main=lessonView();
  else if(state.view==="practice") main=practiceView();
  else if(state.view==="quiz") main=quizView();
  else if(state.view==="cards") main=cardsView();
  else if(state.view==="examples") main=examplesView();
  else if(state.view==="write") main=writeView();
  else if(state.view==="grammar") main=grammarView();
  else if(state.view==="gquiz") main=gquizView();
  else if(state.view==="phrases") main=phrasesView();
  else if(state.view==="phrase") main=phraseStudyView();
  else if(state.view==="pwrite") main=phraseWriteView();
  else if(state.view==="import") main=importView();
  else main=dictionaryView();
  document.documentElement.setAttribute("data-theme", state.theme);
  root.innerHTML=`<div class="wrap">${header()}<main>${main}</main>${nav()}</div>`;
  const sb=document.getElementById("search-box");
  if(sb){ sb.focus(); const v=sb.value; sb.setSelectionRange(v.length,v.length); }
  const tb=document.getElementById("type-box");
  if(tb){
    tb.setAttribute("readonly","readonly");
    tb.addEventListener("pointerdown",()=>{ tb.removeAttribute("readonly"); },{once:true});
  }
}

function toggleLearned(id){
  const ids=state.progress.learnedWordIds.includes(id)
    ? state.progress.learnedWordIds.filter(x=>x!==id)
    : [...state.progress.learnedWordIds,id];
  setProgress({...state.progress, learnedWordIds:ids});
}
function startDayFlow(){
  const mix=Math.random()<0.3;
  state.daySteps=mix
    ? shuffle(["words","spell","grammar","phrases"])
    : ["words","spell","grammar","phrases"];
  state.dayStep=0;
  state.dayFlow=state.daySteps[0];
  runDayStep();
}
function runDayStep(){
  const step=state.daySteps[state.dayStep];
  state.dayFlow=step;
  state.revealed=false; state.choice=null; state.typeAnswer=""; state.typeChecked=null;
  if(step==="words"){
    state.lessonMode="daily"; state.lessonIndex=0; state.view="lesson"; render(); return;
  }
  if(step==="spell"){
    state.spellMode=true; state.quizDir="ru-ro"; startWrite("day"); return;
  }
  if(step==="grammar"){
    state.gq=dayGrammarQs(); state.gqIndex=0; state.gqPicked=null; state.view="gquiz"; render(); return;
  }
  if(step==="phrases"){
    const list=dayPhrases();
    state.quiz=(list.length?list:allPhrases().slice(0,4)).map(p=>({phrase:p}));
    state.quizIndex=0; state.view="phrase"; render(); return;
  }
  finishDayFlow();
}
function nextDayStep(){
  state.dayStep+=1;
  if(state.dayStep>=state.daySteps.length) finishDayFlow();
  else runDayStep();
}
function finishDayFlow(){
  const today=new Date().toISOString().split("T")[0];
  const streak=state.progress.lastStudyDate===today?state.progress.streak:state.progress.streak+1;
  state.dayFlow=null;
  state.view="home";
  setProgress({...state.progress, lastStudyDate:today, streak});
}
function flowNextBtn(label){
  if(!state.dayFlow) return "";
  const left=state.daySteps.slice(state.dayStep+1);
  const names={words:"слова",spell:"написание",grammar:"грамматика",phrases:"фразы"};
  const hint=left.length?("Дальше: "+names[left[0]]):"Конец дня";
  return `<p class="hint">${hint}</p><button data-act="flow-next" class="btn btn-blue">${label||"Дальше"}</button>`;
}
function completeLesson(){
  if(state.dayFlow){ nextDayStep(); return; }
  const today=new Date().toISOString().split("T")[0];
  const streak=state.progress.lastStudyDate===today?state.progress.streak:state.progress.streak+1;
  state.view="home";
  setProgress({...state.progress, lastStudyDate:today, streak});
}
function nextCardOrWrite(ok){
  const cur=state.quiz[state.quizIndex];
  if(cur){ cur.ok=ok; markResult(cur.word.id, ok); }
  state.quizIndex+=1;
  state.revealed=false;
  state.typeAnswer="";
  state.typeChecked=null;
  render();
}

root.addEventListener("click", async (e)=>{
  const dayEl=e.target.closest("[data-day]");
  if(dayEl){
    const d=parseInt(dayEl.getAttribute("data-day"),10);
    if(d){ state.showDays=false; setProgress({...state.progress, currentDay:Math.max(1,d)}); }
    return;
  }
  const exBtn=e.target.closest("[data-ex-act]");
  if(exBtn){
    const i=parseInt(exBtn.getAttribute("data-ex"),10);
    const actEx=exBtn.getAttribute("data-ex-act");
    const w=state.view==="lesson"?lessonWords()[state.lessonIndex]:(state.quiz[state.quizIndex]&&state.quiz[state.quizIndex].word);
    const ex=(w && w.examples && w.examples[i]) || (state.view==="examples" && state.quiz[state.quizIndex] && state.quiz[state.quizIndex].ex);
    if(w && ex) markExample(w.id, ex.ro, actEx==="know");
    return;
  }
  const modeEl=e.target.closest("[data-mode]");
  if(modeEl){ state.importMode=modeEl.getAttribute("data-mode"); return; }
  const dir=e.target.closest("[data-dir]");
  if(dir){ state.quizDir=dir.getAttribute("data-dir"); render(); return; }
  const gopt=e.target.closest("[data-gopt]");
  if(gopt && state.gqPicked===null){
    const i=parseInt(gopt.getAttribute("data-gopt"),10);
    const q=state.gq[state.gqIndex];
    state.gqPicked=i;
    q.ok=i===q.a;
    render();
    return;
  }
  const pick=e.target.closest("[data-pick]");
  if(pick && !state.quizPicked){
    const id=pick.getAttribute("data-pick");
    const q=state.quiz[state.quizIndex];
    state.quizPicked=id;
    const ok=id===q.word.id;
    q.ok=ok;
    markResult(q.word.id, ok);
    return;
  }
  const t=e.target.closest("[data-go],[data-act],[data-speak],[data-toggle],[data-filter]");
  if(!t) return;
  if(t.dataset.speak){ e.stopPropagation(); speakRomanian(decodeURIComponent(t.dataset.speak)); return; }
  if(t.dataset.go){ state.view=t.dataset.go; state.revealed=false; render(); }
  if(t.dataset.toggle) toggleLearned(t.dataset.toggle);
  if(t.dataset.filter){ state.filter=t.dataset.filter; render(); }
  const act=t.dataset.act;
  if(act==="start-day"){ startDayFlow(); }
  if(act==="flow-next"){ nextDayStep(); }
  if(act==="start-daily"){ state.dayFlow=null; state.lessonMode="daily"; state.lessonIndex=0; state.revealed=false; state.choice=null; state.view="lesson"; render(); }
  if(act==="start-review"){ state.lessonMode="review"; state.lessonIndex=0; state.revealed=false; state.view="lesson"; render(); }
  if(act==="reveal"){ state.revealed=true; state.choice=null; render(); }
  if(act==="know"){
    state.choice="know";
    if(state.view==="examples"){
      const item=state.quiz[state.quizIndex];
      if(item && item.ex) markExample(item.word.id, item.ex.ro, true);
    } else render();
  }
  if(act==="unknow"){
    state.choice="unknow";
    if(state.view==="examples"){
      const item=state.quiz[state.quizIndex];
      if(item && item.ex) markExample(item.word.id, item.ex.ro, false);
    } else render();
  }
  if(act==="examples-day") startExamples();
  if(act==="next-word"){
    const words=lessonWords();
    const w=words[state.lessonIndex];
    if(w && state.choice) markResult(w.id, state.choice==="know");
    if(state.lessonIndex>=words.length-1) completeLesson();
    else { state.lessonIndex+=1; state.revealed=false; state.choice=null; render(); }
  }
  if(act==="examples-wrong") startExamples(true);
  if(act==="next-card"){
    const item=state.quiz[state.quizIndex];
    const w=item && item.word;
    if(w && state.choice) markResult(w.id, state.choice==="know");
    state.quizIndex+=1; state.revealed=false; state.choice=null; render();
  }
  if(act==="next-ex"){
    const item=state.quiz[state.quizIndex];
    if(item && item.ex && state.choice) markExample(item.word.id, item.ex.ro, state.choice==="know");
    state.quizIndex+=1; state.revealed=false; state.choice=null; render();
  }
  if(act==="next-day"){
    const next=state.progress.currentDay+1;
    if(state.words.some(w=>w.day===next)) setProgress({...state.progress, currentDay:next});
    else alert("Курс пройден!");
  }
  if(act==="toggle-days"){ state.showDays=!state.showDays; render(); }
  if(act==="theme"){
    state.theme=state.theme==="dark"?"light":"dark";
    localStorage.setItem("romanian_daily_theme", state.theme);
    render();
  }
  if(act==="exam") startExam();
  if(act==="quiz-day"){ state.examMode=false; startQuiz("day"); }
  if(act==="quiz-all"){ state.examMode=false; startQuiz("all"); }
  if(act==="quiz-wrong"){
    state.examMode=false;
    if(!state.progress.wrongIds.length){ state.importMsg=""; state.view="practice"; render(); alert("Ошибок нет — сначала пройди тест."); return; }
    startQuiz("wrong");
  }
  if(act==="cards-day"){ state.examMode=false; startCards("day"); }
  if(act==="write-day"){ state.examMode=false; state.spellMode=false; startWrite("day"); }
  if(act==="spell-day"){ state.examMode=false; state.spellMode=true; state.quizDir="ru-ro"; startWrite("day"); }
  if(act==="qs-n-8"){ state.quizSet={...state.quizSet,n:8}; saveProgress({...state.progress,quizSet:state.quizSet}); render(); }
  if(act==="qs-n-12"){ state.quizSet={...state.quizSet,n:12}; saveProgress({...state.progress,quizSet:state.quizSet}); render(); }
  if(act==="qs-n-20"){ state.quizSet={...state.quizSet,n:20}; saveProgress({...state.progress,quizSet:state.quizSet}); render(); }
  if(act==="qs-c-4"){ state.quizSet={...state.quizSet,choices:4}; saveProgress({...state.progress,quizSet:state.quizSet}); render(); }
  if(act==="qs-c-6"){ state.quizSet={...state.quizSet,choices:6}; saveProgress({...state.progress,quizSet:state.quizSet}); render(); }
  if(act==="qs-mix"){ state.quizSet={...state.quizSet,mix:!state.quizSet.mix}; saveProgress({...state.progress,quizSet:state.quizSet}); render(); }
  if(act==="gquiz") startGrammarQuiz();
  if(act==="gquiz-day"){
    state.gq=dayGrammarQs();
    state.gqIndex=0; state.gqPicked=null; state.view="gquiz"; render();
  }
  if(act==="day-phrases"){
    const list=dayPhrases();
    if(!list.length){ alert("Нет фраз"); return; }
    state.quiz=list.map(p=>({phrase:p}));
    state.quizIndex=0; state.revealed=false; state.choice=null;
    state.view="phrase"; render();
  }
  if(act==="phrase-study") startPhraseStudy();
  if(act==="phrase-write") startPhraseWrite();
  if(act==="pf-all"){ state.phraseFilter="all"; render(); }
  if(act==="pf-home"){ state.phraseFilter="home"; render(); }
  if(act==="pf-out"){ state.phraseFilter="out"; render(); }
  if(act==="phrase-next"){ state.quizIndex+=1; state.revealed=false; state.choice=null; render(); }
  if(act==="dia" || act==="backspace"){
    const box=document.getElementById("type-box");
    if(box && state.typeChecked===null){
      if(act==="backspace") box.value=(box.value||"").slice(0,-1);
      else box.value=(box.value||"")+(t.getAttribute("data-ch")||"");
      state.typeAnswer=box.value;
      const preview=document.querySelector(".typed");
      if(preview) preview.textContent=box.value || "";
    }
  }
  if(act==="check-phrase"){
    const box=document.getElementById("type-box");
    state.typeAnswer=box?box.value:"";
    const ph=state.quiz[state.quizIndex].phrase;
    const ok=similar(state.typeAnswer, ph.ro);
    state.typeChecked=ok;
    state.quiz[state.quizIndex].ok=ok;
    render();
  }
  if(act==="pwrite-next"){
    state.quizIndex+=1; state.typeAnswer=""; state.typeChecked=null; render();
  }
  if(act==="gquiz-next"){ state.gqIndex+=1; state.gqPicked=null; render(); }
  if(act==="import-phrases"){
    const box=document.getElementById("paste-phrases");
    const lines=(box?box.value:"").split(/\n+/).map(s=>s.trim()).filter(Boolean);
    const extra=[];
    for(const line of lines){
      const m=line.split(/\s+[—\-–]\s+|\s+-\s+/);
      if(m.length>=2) extra.push({id:"p-"+Date.now()+"-"+extra.length, place:"own", ro:m[0].trim(), ru:m.slice(1).join(" - ").trim()});
    }
    if(!extra.length){ alert("Формат: frază — перевод"); return; }
    let saved=[];
    try{ saved=JSON.parse(localStorage.getItem("romanian_daily_phrases")||"[]"); }catch(e){}
    localStorage.setItem("romanian_daily_phrases", JSON.stringify(saved.concat(extra)));
    render();
  }
  if(act==="quiz-next"){ state.quizIndex+=1; state.quizPicked=null; render(); }
  if(act==="retry-wrong"){
    const miss=state.quiz.filter(q=>q.ok===false).map(q=>q.word);
    if(!miss.length){ state.view="practice"; render(); return; }
    state.quiz=buildQuiz(miss, miss.length); state.quizIndex=0; state.quizPicked=null; render();
  }
  if(act==="check-type"){
    const box=document.getElementById("type-box");
    state.typeAnswer=box?box.value:"";
    const w=state.quiz[state.quizIndex].word;
    const expect=(state.spellMode||state.quizDir!=="ro-ru")?w.word:w.translation;
    const ok=similar(state.typeAnswer, expect);
    state.typeChecked=ok;
    state.quiz[state.quizIndex].ok=ok;
    markResult(w.id, ok);
    return;
  }
  if(act==="more-hint"){ state.hintLevel=Math.min(2,(state.hintLevel||0)+1); render(); }
  if(act==="write-next"){ state.hintLevel=0; nextCardOrWrite(state.quiz[state.quizIndex] && state.quiz[state.quizIndex].ok); }
  if(act==="import-paste"){
    const box=document.getElementById("paste-words");
    const text=box?box.value:"";
    try{
      let list=parseLineWords(text.split(/\n+/));
      if(!list.length) list=parseCsv(text);
      if(!list.length) list=parseNarrativeLines(text.split(/\n+/));
      const n=finalizeWords(list, state.importMode);
      state.importMsg="Загружено слов: "+n+". Всего: "+state.words.length;
    }catch(err){ state.importMsg="Ошибка: "+(err.message||err); }
    render();
  }
  if(act==="reset-words"){
    localStorage.removeItem(WORDS_KEY);
    state.words=WORDS;
    state.importMsg="Вернул курс из файла ("+WORDS.length+" слов)";
    setProgress({learnedWordIds:[], streak:state.progress.streak, lastStudyDate:state.progress.lastStudyDate, currentDay:1, wrongIds:[], stats:{attempts:0,correct:0}, exampleMarks:{}});
  }
});
root.addEventListener("change",(e)=>{
  if(e.target && e.target.name==="imode") state.importMode=e.target.getAttribute("data-mode")||"append";
  if(e.target && e.target.id==="file-input" && e.target.files && e.target.files[0]) handleFile(e.target.files[0]);
});
root.addEventListener("input",(e)=>{
  if(e.target && e.target.id==="search-box"){ state.search=e.target.value; render(); }
  if(e.target && e.target.id==="type-box"){
    state.typeAnswer=e.target.value;
    const preview=document.querySelector(".typed");
    if(preview) preview.textContent=e.target.value;
  }
});
root.addEventListener("keydown",(e)=>{
  if(e.key==="Enter" && e.target && e.target.id==="type-box"){
    e.preventDefault();
    document.querySelector("[data-act=check-type],[data-act=write-next]")?.click();
  }
});

function finalizeWords(list, mode){
  const incoming=list.map((w,i)=>({
    id:"c-"+Date.now()+"-"+i,
    word:w.word,
    translation:w.translation||w.word,
    transcription:w.transcription||"",
    day:w.day,
    examples:w.examples||[]
  }));
  let base=mode==="replace"?[]:state.words.slice();
  const seen=new Set(base.map(w=>w.word.toLowerCase()));
  const added=[];
  for(const w of incoming){
    if(!w.word || seen.has(w.word.toLowerCase())) continue;
    seen.add(w.word.toLowerCase());
    added.push(w);
  }
  let startDay=1;
  if(mode==="append" && base.length){
    startDay=Math.max(...base.map(w=>w.day||1));
    if(base.filter(w=>w.day===startDay).length>=10) startDay+=1;
  }
  let cursor=startDay, inDay=mode==="append"?base.filter(w=>w.day===cursor).length:0;
  for(const w of added){
    if(w.day) continue;
    if(inDay>=10){ cursor+=1; inDay=0; }
    w.day=cursor; inDay+=1;
  }
  for(const w of added){ if(!w.day||w.day<1) w.day=1; }
  const next=base.concat(added);
  if(!next.length) throw new Error("Не нашёл ни одного слова");
  state.words=next;
  localStorage.setItem(WORDS_KEY, JSON.stringify(next));
  if(mode==="replace") setProgress({learnedWordIds:[],streak:state.progress.streak,lastStudyDate:state.progress.lastStudyDate,currentDay:1,wrongIds:[],stats:{attempts:0,correct:0}});
  else render();
  return added.length;
}


function u16(u,i){return u[i]|u[i+1]<<8}
function u32(u,i){return (u[i]|u[i+1]<<8|u[i+2]<<16|u[i+3]<<24)>>>0}

async function inflateRaw(data){
  if(typeof DecompressionStream==="undefined") throw new Error("Этот браузер не читает Excel/Word. Вставь текст или CSV.");
  const ds=new DecompressionStream("deflate-raw");
  return new Uint8Array(await new Response(new Blob([data]).stream().pipeThrough(ds)).arrayBuffer());
}
async function unzip(buf){
  const u=new Uint8Array(buf);
  const files={};
  let i=0;
  while(i+4<=u.length){
    if(u32(u,i)===0x02014b50) break;
    i++;
  }
  if(i+4>u.length){
    i=0;
    while(i+30<=u.length){
      if(u32(u,i)!==0x04034b50) break;
      const method=u16(u,i+8);
      const compSize=u32(u,i+18);
      const nameLen=u16(u,i+26);
      const extraLen=u16(u,i+28);
      const name=new TextDecoder("utf-8").decode(u.slice(i+30,i+30+nameLen));
      const start=i+30+nameLen+extraLen;
      const data=u.slice(start,start+compSize);
      if(method===0) files[name]=data;
      else if(method===8 && data.length) files[name]=await inflateRaw(data);
      i=start+compSize;
    }
    return files;
  }
  while(i+46<=u.length && u32(u,i)===0x02014b50){
    const method=u16(u,i+10);
    const compSize=u32(u,i+20);
    const nameLen=u16(u,i+28);
    const extraLen=u16(u,i+30);
    const commentLen=u16(u,i+32);
    const localOff=u32(u,i+42);
    const name=new TextDecoder("utf-8").decode(u.slice(i+46,i+46+nameLen));
    const localNameLen=u16(u,localOff+26);
    const localExtra=u16(u,localOff+28);
    const start=localOff+30+localNameLen+localExtra;
    const data=u.slice(start,start+compSize);
    if(method===0) files[name]=data;
    else if(method===8 && data.length) files[name]=await inflateRaw(data);
    i+=46+nameLen+extraLen+commentLen;
  }
  return files;
}
function decodeXml(bytes){
  let text=new TextDecoder("utf-8").decode(bytes);
  if(text.charCodeAt(0)===0xFEFF) text=text.slice(1);
  return text;
}
function xmlUnescape(s){
  return s.replace(/&amp;/g,"&").replace(/&lt;/g,"<").replace(/&gt;/g,">").replace(/&quot;/g,'"').replace(/&apos;/g,"'");
}
function colRow(ref){
  const m=/^([A-Z]+)(\d+)$/.exec(ref||"");
  if(!m) return [0,0];
  let col=0;
  for(const ch of m[1]) col=col*26+(ch.charCodeAt(0)-64);
  return [col-1, parseInt(m[2],10)-1];
}
function parseSharedStrings(xml){
  const out=[];
  const parts=xml.split(/<si[ >]/).slice(1);
  for(const part of parts){
    const texts=[...part.matchAll(/<t[^>]*>([^<]*)<\/t>/g)].map(m=>xmlUnescape(m[1]));
    out.push(texts.join(""));
  }
  return out;
}
function parseSheet(xml, strings){
  const rows=[];
  const cells=[...xml.matchAll(/<c\s([^>]*?)(?:\/>|>([\s\S]*?)<\/c>)/g)];
  for(const m of cells){
    const attrs=m[1];
    const inner=m[2]||"";
    const ref=(/r="([^"]+)"/.exec(attrs)||[])[1];
    if(!ref) continue;
    const t=(/t="([^"]+)"/.exec(attrs)||[])[1]||"";
    const [c,r]=colRow(ref);
    let val="";
    if(t==="s"){
      const v=(/<v>([^<]*)<\/v>/.exec(inner)||[])[1];
      val=strings[parseInt(v,10)]||"";
    } else if(t==="inlineStr"){
      val=[...inner.matchAll(/<t[^>]*>([^<]*)<\/t>/g)].map(x=>xmlUnescape(x[1])).join("");
    } else {
      const v=(/<v>([^<]*)<\/v>/.exec(inner)||[])[1];
      val=v!=null?xmlUnescape(v):"";
    }
    if(!rows[r]) rows[r]=[];
    rows[r][c]=val;
  }
  return rows.map(row=>row||[]);
}
function normHead(s){
  return String(s||"").trim().toLowerCase().replace(/\s+/g," ");
}
function mapHeader(h){
  const s=normHead(h);
  if(["word","слово","romanian","română","romana","ro","cuvânt","cuvant"].includes(s) || s.startsWith("слово")) return "word";
  if(["translation","перевод","русский","ru","meaning","перевод на русский"].includes(s) || s.startsWith("перевод")) return "translation";
  if(["transcription","транскрипция","произношение","ipa"].includes(s)) return "transcription";
  if(["day","день","zi"].includes(s)) return "day";
  if(["example_ro","пример ro","пример_ro","example","пример"].includes(s)) return "example_ro";
  if(["example_ru","пример ru","пример_ru"].includes(s)) return "example_ru";
  if(["example_transcription","транскрипция примера"].includes(s)) return "example_transcription";
  return null;
}
function rowsToWords(rows){
  if(!rows.length) return [];
  let start=0;
  let map={0:"word",1:"translation",2:"transcription",3:"day",4:"example_ro",5:"example_ru",6:"example_transcription"};
  const first=rows[0].map(mapHeader);
  if(first.some(Boolean)){
    map={};
    first.forEach((k,i)=>{ if(k) map[i]=k; });
    start=1;
    if(rows[1] && rows[1].every(x=>!x || /слово|перевод|подсказ|транскрип/i.test(String(x)))) start=2;
  }
  const words=[];
  for(let i=start;i<rows.length;i++){
    const row=rows[i]||[];
    if(!row.some(x=>String(x||"").trim())) continue;
    const obj={};
    Object.entries(map).forEach(([idx,key])=>{ obj[key]=String(row[idx]??"").trim(); });
    if(!obj.word) continue;
    if(!obj.translation) obj.translation=obj.word;
    const day=parseInt(obj.day,10);
    const examples=[];
    if(obj.example_ro){
      examples.push({ro:obj.example_ro, ru:obj.example_ru||"", transcription:obj.example_transcription||""});
    }
    words.push({
      word: obj.word,
      translation: obj.translation,
      transcription: obj.transcription||"",
      day: Number.isFinite(day)&&day>0?day:null,
      examples
    });
  }
  return words;
}
function parseCsv(text){
  text=text.replace(/^\uFEFF/,"");
  const delim=(text.split("\n")[0].match(/;/g)||[]).length>(text.split("\n")[0].match(/,/g)||[]).length?";":",";
  const rows=[];
  let row=[], cur="", q=false;
  for(let i=0;i<text.length;i++){
    const ch=text[i];
    if(q){
      if(ch=='"' && text[i+1]=='"'){ cur+='"'; i++; }
      else if(ch=='"') q=false;
      else cur+=ch;
    } else {
      if(ch=='"') q=true;
      else if(ch===delim){ row.push(cur); cur=""; }
      else if(ch==="\n"){ row.push(cur); rows.push(row); row=[]; cur=""; }
      else if(ch!=="\r") cur+=ch;
    }
  }
  if(cur.length||row.length) { row.push(cur); rows.push(row); }
  return rowsToWords(rows);
}
function parseLineWords(lines){
  const words=[];
  for(const raw of lines){
    const line=raw.replace(/\s+/g," ").trim();
    if(!line || /шаблон|формате|пример|заполните|сохраните|колонк/i.test(line)) continue;
    let m=line.match(/^(.+?)\s*[—–\-]\s*(.+?)(?:\s*\[(.+?)\])?$/);
    if(!m) m=line.match(/^([^,;|]+)\s*[,;|]\s*([^,;|\[]+)(?:\s*\[(.+?)\])?$/);
    if(!m) continue;
    words.push({word:m[1].trim(), translation:m[2].trim(), transcription:(m[3]||"").trim(), day:null, examples:[]});
  }
  return words;
}
function parseNarrativeLines(lines){
  const words=[];
  let day=1, cur=null;
  const flush=()=>{ if(cur&&cur.word) words.push(cur); cur=null; };
  for(let raw of lines){
    const line=String(raw||"").replace(/\uF0B7/g,"•").trim();
    if(!line) continue;
    let m=line.match(/^День\s+(\d+)/i);
    if(m){ flush(); day=parseInt(m[1],10); continue; }
    m=line.match(/^\d+\.\s*Слово:\s*(.+)$/i);
    if(m){
      flush();
      cur={word:m[1].trim(), translation:"", transcription:"", day, examples:[]};
      continue;
    }
    if(!cur) continue;
    m=line.match(/^Перевод:\s*(.+)$/i);
    if(m){ cur.translation=m[1].trim(); continue; }
    m=line.match(/^Транскрипция:\s*(.+)$/i);
    if(m){ cur.transcription=m[1].trim(); continue; }
    if(/^Переложения/i.test(line)) continue;
    const cleaned=line.replace(/^[•\-\*]+\s*/,"");
    const em=cleaned.match(/^(.+?)\s*\((.+)\)\s*$/);
    if(em) cur.examples.push({ru:em[1].trim(), ro:em[2].trim(), transcription:cur.transcription||""});
  }
  flush();
  return words;
}
async function parseXlsx(buf){
  const files=await unzip(buf);
  const sheetName=Object.keys(files).find(n=>/xl\/worksheets\/sheet1\.xml$/i.test(n))
    || Object.keys(files).find(n=>/xl\/worksheets\/sheet\d+\.xml$/i.test(n));
  const ssName=Object.keys(files).find(n=>/xl\/sharedStrings\.xml$/i.test(n));
  if(!sheetName) throw new Error("В Excel нет листа");
  const strings=ssName?parseSharedStrings(decodeXml(files[ssName])):[];
  const rows=parseSheet(decodeXml(files[sheetName]), strings);
  let list=rowsToWords(rows);
  if(list.length<5){
    const lines=[];
    for(const row of rows){
      for(const cell of row){ if(String(cell||"").trim()) lines.push(String(cell).trim()); }
    }
    list=parseNarrativeLines(lines);
  }
  return list;
}
async function parseDocx(buf){
  const files=await unzip(buf);
  const name=Object.keys(files).find(n=>n==="word/document.xml");
  if(!name) throw new Error("Это не Word .docx");
  const xml=decodeXml(files[name]);
  const tableRows=[];
  const trs=xml.split(/<w:tr[ >]/).slice(1);
  for(const tr of trs){
    const cells=[...tr.matchAll(/<w:tc[ >][\s\S]*?<\/w:tc>/g)].map(tc=>{
      return [...tc[0].matchAll(/<w:t[^>]*>([^<]*)<\/w:t>/g)].map(x=>xmlUnescape(x[1])).join("").trim();
    });
    if(cells.some(Boolean)) tableRows.push(cells);
  }
  let words=tableRows.length>=2?rowsToWords(tableRows):[];
  if(!words.length){
    const paras=xml.split(/<w:p[ >]/).slice(1).map(p=>[...p.matchAll(/<w:t[^>]*>([^<]*)<\/w:t>/g)].map(x=>xmlUnescape(x[1])).join(""));
    words=parseLineWords(paras);
  }
  return words;
}
function finalizeWords(list, mode){
  const incoming=list.map((w,i)=>({
    id: "c-"+Date.now()+"-"+i+"-"+Math.random().toString(36).slice(2,6),
    word: w.word,
    translation: w.translation,
    transcription: w.transcription||"",
    day: w.day,
    examples: w.examples||[]
  }));
  let base=mode==="replace"?[]:state.words.slice();
  const existing=new Set(base.map(w=>w.word.toLowerCase()));
  const added=[];
  for(const w of incoming){
    if(existing.has(w.word.toLowerCase())) continue;
    existing.add(w.word.toLowerCase());
    added.push(w);
  }
  let startDay=1;
  if(mode==="append" && base.length){
    startDay=Math.max(...base.map(w=>w.day||1));
    const lastCount=base.filter(w=>w.day===startDay).length;
    if(lastCount>=10) startDay+=1;
  }
  let cursor=startDay;
  let inDay=mode==="append"?base.filter(w=>w.day===cursor).length:0;
  for(const w of added){
    if(w.day) continue;
    if(inDay>=10){ cursor+=1; inDay=0; }
    w.day=cursor;
    inDay+=1;
  }
  for(const w of added){ if(!w.day) w.day=1; }
  const next=base.concat(added);
  if(!next.length) throw new Error("Не нашёл ни одного слова");
  state.words=next;
  localStorage.setItem(WORDS_KEY, JSON.stringify(next));
  if(mode==="replace"){
    setProgress({learnedWordIds:[], streak:state.progress.streak, lastStudyDate:state.progress.lastStudyDate, currentDay:1});
  } else {
    render();
  }
  return added.length;
}
function downloadCsvTemplate(){
  const csv="\uFEFFword,translation,transcription,day,example_ro,example_ru,example_transcription\nслово (румынский),перевод,транскрипция,день,пример RO,пример RU,транскрипция примера\nbună,привет / здравствуй,бу-нэ,1,Bună! Ce mai faci?,Привет! Как дела?,Бунэ! Че май фачи?\nmulțumesc,спасибо,мул-цу-меск,1,Mulțumesc frumos!,Большое спасибо!,Мулцумеск фрумос!\n";
  const a=document.createElement("a");
  a.href=URL.createObjectURL(new Blob([csv],{type:"text/csv;charset=utf-8"}));
  a.download="romanian-daily-shablon.csv";
  a.click();
}
async function handleFile(file){
  try{
    const name=(file.name||"").toLowerCase();
    let list=[];
    if(name.endsWith(".csv")||name.endsWith(".txt")||name.endsWith(".tsv")){
      list=parseCsv(await file.text());
    } else if(name.endsWith(".xlsx")||name.endsWith(".xls")){
      if(name.endsWith(".xls") && !name.endsWith(".xlsx")) throw new Error("Нужен .xlsx (Excel → Сохранить как → Книга Excel)");
      list=await parseXlsx(await file.arrayBuffer());
    } else if(name.endsWith(".docx")){
      list=await parseDocx(await file.arrayBuffer());
    } else {
      throw new Error("Формат не поддерживается. Нужен .xlsx, .docx или .csv");
    }
    const n=finalizeWords(list, state.importMode);
    state.importMsg="Загружено слов: "+n+". Всего в курсе: "+state.words.length;
  }catch(e){
    state.importMsg="Ошибка: "+(e.message||e);
  }
  render();
}

/* ---------------------------------------------------------------- загрузка данных */
/* Данные лежат в отдельных JSON: words-partN.json, grammar.json, quiz.json, phrases.json.
   В одиночной офлайн-сборке они встроены в HTML — тогда fetch не нужен. */
const PACK_MAX = 12;

async function loadJSON(url, fallback){
  try{
    const res = await fetch(url);
    if(!res.ok) return fallback;
    return await res.json();
  }catch(e){
    return fallback;
  }
}

async function loadWordPacks(){
  const packs = [];
  for(let i=1;i<=PACK_MAX;i+=1){
    const res = await fetch("./words-part"+i+".json");
    if(res.status===404) break;           // паки кончились — это нормально
    if(!res.ok) throw new Error("words-part"+i+".json: сервер ответил "+res.status);
    packs.push(await res.json());
  }
  return packs.flat();
}

async function loadCourseData(){
  const inline = window.__ROMANIAN_DAILY_DATA__;
  if(inline){
    WORDS = inline.words || [];
    GRAMMAR = inline.grammar || {};
    SENTENCES = inline.phrases || [];
    GRAMMAR_Q = inline.quiz || [];
  } else {
    const [words, grammar, phrases, quiz] = await Promise.all([
      loadWordPacks(),
      loadJSON("./grammar.json", {}),
      loadJSON("./phrases.json", []),
      loadJSON("./quiz.json", [])
    ]);
    WORDS = words; GRAMMAR = grammar; SENTENCES = phrases; GRAMMAR_Q = quiz;
  }
  if(!Array.isArray(WORDS) || !WORDS.length) throw new Error("Не найден ни один words-partN.json");
}

function errorView(){
  return `<div class="page">
    <div class="hero">
      <p class="kicker">Romanian Daily</p>
      <h2>Данные не загрузились</h2>
      <p>${esc(state.loadError || "Неизвестная ошибка")}</p>
      <button data-act="reload" class="btn btn-blue">Попробовать снова</button>
    </div>
  </div>`;
}

function render(){
  if(state.loadError){ root.innerHTML=errorView(); return; }
  renderApp();
}

root.addEventListener("click",(e)=>{
  if(e.target.closest('[data-act="reload"]')) location.reload();
});

(async function init(){
  // Офлайн-кэш: без него установленное приложение не откроется без сети
  if("serviceWorker" in navigator){
    navigator.serviceWorker.register("./sw.js").catch((e)=>console.warn("SW не зарегистрирован", e));
  }
  try{
    await loadCourseData();
  }catch(e){
    state.loadError = e.message || String(e);
    console.error(e);
  }
  state.words = loadSavedWords() || WORDS;
  render();
})();
