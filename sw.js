/* Service worker: офлайн-работа приложения. */
const CACHE = "romanian-daily-v8";

const CORE = [
  "./",
  "./index.html",
  "./app.js",
  "./app-srs.js",
  "./styles.css",
  "./manifest.json",
  "./icon.svg",
  "./icon-192.png",
  "./icon-512.png",
  "./words-part1.json",
  "./words-part2.json",
  "./words-part3.json",
  "./words-part4.json",
  "./words-part5.json",
  "./grammar.json",
  "./quiz.json",
  "./phrases.json",
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
  if (event.data && event.data.type === "SKIP_WAITING") self.skipWaiting();
});

self.addEventListener("fetch", (event) => {
  const req = event.request;
  if (req.method !== "GET") return;
  event.respondWith(handle(req));
});

async function handle(req) {
  const url = new URL(req.url);
  if (url.origin !== self.location.origin) return fetch(req);
  if (req.mode === "navigate") {
    try {
      const net = await fetch(req);
      const c = await caches.open(CACHE);
      c.put("./index.html", net.clone());
      return net;
    } catch {
      return (await caches.match("./index.html")) || (await caches.match("./")) || Response.error();
    }
  }
  const hit = await caches.match(req);
  if (hit) {
    fetch(req).then((res) => {
      if (res && res.ok) caches.open(CACHE).then((c) => c.put(req, res));
    }).catch(() => {});
    return hit;
  }
  try {
    const net = await fetch(req);
    if (net && net.ok) {
      const c = await caches.open(CACHE);
      c.put(req, net.clone());
    }
    return net;
  } catch {
    return Response.error();
  }
}

async function cache(url) {
  const c = await caches.open(CACHE);
  const res = await fetch(url, { cache: "no-store" });
  if (!res.ok) throw new Error(url);
  await c.put(url, res);
}
