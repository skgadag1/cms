export const DEVICES = ["mobile", "tablet", "desktop"];
export const DEFAULT_DEVICE = "desktop";
export const BG_COLORS = ["none", "black", "white", "gray-lighter"];
export const TILE_OPTIONS = ["img", "imgBg", "title"];

export function parseLayout(value, fallbackFactory) {
  if (!value) return fallbackFactory();
  try {
    const parsed = JSON.parse(value);
    if (parsed && Array.isArray(parsed.tiles)) return parsed;
  } catch {
    // fall through
  }
  return fallbackFactory();
}

export function nextAvailableId(tiles) {
  const used = new Set(
    tiles.map((tile) => Number(tile.id)).filter((n) => !Number.isNaN(n)),
  );
  let id = 1;
  while (used.has(id)) id++;
  return String(id);
}

export function escapeHtml(str = "") {
  const div = document.createElement("div");
  div.textContent = str;
  return div.innerHTML;
}

export function normalizeTileResponsive(tile) {
  const { content, text, x, y, w, h, ...rest } = tile;
  const before =
    typeof tile.before === "string"
      ? tile.before
      : typeof text === "string"
        ? text
        : content || "";
  const after = typeof tile.after === "string" ? tile.after : "";
  const baseX = Number.isFinite(Number(x)) ? Number(x) : 0;
  const baseY = Number.isFinite(Number(y)) ? Number(y) : 0;
  const baseW = Number(w) > 0 ? Number(w) : 1;
  const baseH = Number(h) > 0 ? Number(h) : 1;
  const responsive = tile.responsive || {};

  const normalized = {};
  DEVICES.forEach((device) => {
    const source = responsive[device] || {};
    normalized[device] = {
      x: Number.isFinite(Number(source.x)) ? Number(source.x) : baseX,
      y: Number.isFinite(Number(source.y)) ? Number(source.y) : baseY,
      w: Number(source.w) > 0 ? Number(source.w) : baseW,
      h: Number(source.h) > 0 ? Number(source.h) : baseH,
    };
  });

  return {
    ...rest,
    tileOption: TILE_OPTIONS.includes(tile.tileOption)
      ? tile.tileOption
      : "title",
    contentTitle:
      typeof tile.contentTitle === "string" ? tile.contentTitle : "",
    img: typeof tile.img === "string" ? tile.img : "",
    imBg: typeof tile.imBg === "string" ? tile.imBg : "",
    bgColor: BG_COLORS.includes(tile.bgColor) ? tile.bgColor : "none",
    before,
    after,
    responsive: normalized,
  };
}

export function normalizeLayout(layout) {
  return {
    tiles: (layout.tiles || []).map((tile) => normalizeTileResponsive(tile)),
  };
}

export function getTileDeviceSettings(tile, device) {
  const normalized = normalizeTileResponsive(tile);
  return normalized.responsive[device] || normalized.responsive[DEFAULT_DEVICE];
}

function isEmptyRichText(html) {
  if (typeof html !== "string" || !html) return true;
  const div = document.createElement("div");
  div.innerHTML = html;
  return !div.textContent.trim();
}

export function makeWidgetHtml(tile, device = DEFAULT_DEVICE) {
  const bgColorClass =
    BG_COLORS.includes(tile.bgColor) && tile.bgColor !== "none"
      ? ` bl__tile-bg--${tile.bgColor}`
      : "";
  const beforeHtml = typeof tile.before === "string" ? tile.before : "";
  const afterHtml = typeof tile.after === "string" ? tile.after : "";
  const tileOption = TILE_OPTIONS.includes(tile.tileOption)
    ? tile.tileOption
    : "title";

  const bgImageSrc = tileOption === "imgBg" && tile.imBg ? tile.imBg : "";
  const bgImageHtml = bgImageSrc
    ? `<img class="bl__tile-img" src="${escapeHtml(bgImageSrc)}" alt="" />`
    : "";

  const fgImageSrc = tileOption === "img" && tile.img ? tile.img : "";
  const fgImageHtml = fgImageSrc
    ? `<img class="bl__tile-content-img" src="${escapeHtml(fgImageSrc)}" alt="" />`
    : "";

  const titleHtml =
    tileOption === "title" && tile.contentTitle ? tile.contentTitle : "";
  const titleBlock =
    titleHtml && !isEmptyRichText(titleHtml)
      ? `<div class="bl__tile-title">${titleHtml}</div>`
      : "";

  const beforeBlock = isEmptyRichText(beforeHtml)
    ? ""
    : `<div class="bl__tile-before">${beforeHtml}</div>`;
  const afterBlock = isEmptyRichText(afterHtml)
    ? ""
    : `<div class="bl__tile-after">${afterHtml}</div>`;

  return `<div class="bl__tile-inner${bgImageSrc ? " bl__tile-inner--has-image" : ""}${bgColorClass}">
    ${bgImageHtml}
    <button class="bl__tile-edit" data-tile-id="${tile.id}" title="Edit content">
      <svg viewBox="0 0 16 16" width="14" height="14" fill="currentColor"><path d="M11.13 1.47a1.5 1.5 0 0 1 2.12 0l1.28 1.28a1.5 1.5 0 0 1 0 2.12L5.9 13.5a1 1 0 0 1-.45.26l-3.5 1a.5.5 0 0 1-.62-.62l1-3.5a1 1 0 0 1 .26-.45L11.13 1.47ZM12.2 2.53 3.56 11.17l-.66 2.33 2.33-.66L13.87 4.2 12.2 2.53Z"/></svg>
    </button>
    <div class="bl__tile-text">
      <div class="bl__tile-fit">
        ${beforeBlock}
        ${titleBlock}
        ${fgImageHtml}
        ${afterBlock}
      </div>
    </div>
    <button class="bl__tile-remove" data-tile-id="${tile.id}" title="Remove tile">\u00d7</button>
  </div>`;
}
