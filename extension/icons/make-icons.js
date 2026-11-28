// Writes icon16/32/48/128.png next to this file. Same geometry as
// generate-icons.html, rasterised in plain Node (no canvas dependency).
//   node extension/icons/make-icons.js
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const BG = [0x0a, 0x08, 0x16];
const MOON = [0xc0, 0x90, 0xf0];
const SS = 8; // supersamples per axis

function inRoundedRect(x, y, s) {
  const r = s * 0.22;
  const cx = Math.min(Math.max(x, r), s - r);
  const cy = Math.min(Math.max(y, r), s - r);
  return (x - cx) ** 2 + (y - cy) ** 2 <= r * r && x >= 0 && y >= 0 && x <= s && y <= s;
}

function inMoon(x, y, s) {
  const cx = s * 0.52, cy = s * 0.5, R = s * 0.3;
  const inFull = (x - cx) ** 2 + (y - cy) ** 2 <= R * R;
  const kx = cx + R * 0.42, ky = cy - R * 0.08, kr = R * 0.78;
  const inCut = (x - kx) ** 2 + (y - ky) ** 2 <= kr * kr;
  return inFull && !inCut;
}

function render(size) {
  const px = Buffer.alloc(size * size * 4);
  for (let py = 0; py < size; py++) {
    for (let pxl = 0; pxl < size; pxl++) {
      let bg = 0, moon = 0;
      for (let sy = 0; sy < SS; sy++) {
        for (let sx = 0; sx < SS; sx++) {
          const x = pxl + (sx + 0.5) / SS, y = py + (sy + 0.5) / SS;
          if (!inRoundedRect(x, y, size)) continue;
          if (inMoon(x, y, size)) moon++; else bg++;
        }
      }
      const cover = bg + moon;
      const i = (py * size + pxl) * 4;
      if (cover === 0) continue;
      for (let c = 0; c < 3; c++) px[i + c] = Math.round((BG[c] * bg + MOON[c] * moon) / cover);
      px[i + 3] = Math.round((255 * cover) / (SS * SS));
    }
  }
  return px;
}

const CRC_TABLE = Array.from({ length: 256 }, (_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});
function crc32(buf) {
  let c = 0xffffffff;
  for (const b of buf) c = CRC_TABLE[(c ^ b) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}
function chunk(type, data) {
  const len = Buffer.alloc(4); len.writeUInt32BE(data.length);
  const td = Buffer.concat([Buffer.from(type), data]);
  const crc = Buffer.alloc(4); crc.writeUInt32BE(crc32(td));
  return Buffer.concat([len, td, crc]);
}
function png(size, rgba) {
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(size, 0); ihdr.writeUInt32BE(size, 4);
  ihdr[8] = 8; ihdr[9] = 6; // 8-bit RGBA
  const raw = Buffer.alloc(size * (size * 4 + 1));
  for (let y = 0; y < size; y++) rgba.copy(raw, y * (size * 4 + 1) + 1, y * size * 4, (y + 1) * size * 4);
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', ihdr),
    chunk('IDAT', zlib.deflateSync(raw)),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}

for (const size of [16, 32, 48, 128]) {
  const out = path.join(__dirname, `icon${size}.png`);
  fs.writeFileSync(out, png(size, render(size)));
  console.log('wrote', out);
}
