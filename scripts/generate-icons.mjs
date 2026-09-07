#!/usr/bin/env node
/* Генерирует PNG-иконки из icon.svg (нужны для установки PWA на Android/iOS). */
import { mkdir, readFile } from "node:fs/promises";
import sharp from "sharp";

const ROOT = new URL("..", import.meta.url).pathname;
const svg = await readFile(new URL("../icon.svg", import.meta.url));
const outDir = new URL("../", import.meta.url).pathname;

await mkdir(outDir, { recursive: true });

async function write(name, size, { maskable = false } = {}) {
  const glyph = await sharp(svg)
    .resize(Math.round(size * (maskable ? 0.62 : 0.82)), null, { fit: "inside" })
    .toBuffer();

  const canvas = maskable
    ? sharp({ create: { width: size, height: size, channels: 4, background: "#2563eb" } })
    : sharp({ create: { width: size, height: size, channels: 4, background: "#00000000" } });

  const file = `${outDir}${name}`;
  await canvas.composite([{ input: glyph, gravity: "center" }]).png().toFile(file);
  console.log(`${name} → ${size}×${size}${maskable ? " (maskable)" : ""}`);
}

await write("icon-192.png", 192);
await write("icon-512.png", 512);
await write("icon-maskable-512.png", 512, { maskable: true });
