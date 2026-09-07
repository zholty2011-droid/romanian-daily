/* Service worker: офлайн-работа приложения. */
const CACHE = "romanian-daily-v5";

/* Минимум, без которого приложение не стартует. Есть в репозитории — важно держать список актуальным. */
const CORE = [
  "./",
  "./index.html",
  "./app.js",
  "./styles.css",
  "./manifest.json",
  "./icon.svg",
  "./icon-192.png",
  "./icon-512.png",
  "./words-part1.json",
  "./words-part2.json",
  "./words-part3.json",
];

self.addEventListener("install", (event) => {
  event.waitUntil(
    Promise.allSettled(CORE.map((url) => cache(url)))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener("message", (event) => {
  if (event.data === "SKIP_WAITING") self.skipWaiting();
});

async function cache(url) {
  const c = await caches.open(CACHE);
  const res = await fetch(url, { cache: "reload" });
  if (!res || !res.ok) throw new Error(`Не удалось закэшировать ${url}: ${res?.status}`);
  await c.put(url, res);
}

/** Навигация: сначала сеть (чтобы быстро получать обновления), при офлайне — кэш. */
async function networkFirst(request) {
  try {
    const res = await fetch(request);
    if (res && res.status === 200) {
      const c = await caches.open(CACHE);
      c.put(request, res.clone());
    }
    return res;
  } catch (e) {
    const cached = await caches.match(request);
    return cached || (await caches.match("./index.html")) || Response.error();
  }
}

/** Ресурсы: отдаём из кэша и параллельно обновляем (stale-while-revalidate). */
async function staleWhileRevalidate(request) {
  const c = await caches.open(CACHE);
  const cached = await c.match(request);
  const network = fetch(request)
    .then((res) => {
      if (res && res.status === 200) c.put(request, res.clone());
      return res;
    })
    .catch(() => null);
  return cached || (await network) || Response.error();
}

self.addEventListener("fetch", (event) => {
  const { request } = event;
  if (request.method !== "GET") return;

  const url = new URL(request.url);
  if (url.origin !== self.location.origin) return; // CDN, аналитика и т.п. — мимо

  if (request.mode === "navigate") {
    event.respondWith(networkFirst(request));
    return;
  }
  event.respondWith(staleWhileRevalidate(request));
});
