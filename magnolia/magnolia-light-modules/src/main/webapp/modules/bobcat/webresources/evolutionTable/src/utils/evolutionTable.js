export const CELL_TYPES = ["empty", "check", "dash", "text"];
export const COLOR_OPTIONS = [
  "default",
  "orange",
  "red",
  "white",
  "black",
];
export const LINK_TYPES = ["none", "page", "external"];
export const LINK_TARGETS = ["_self", "_blank"];
export const TABLE_COLOR_SCHEMES = ["black", "white"];
export const BACKGROUND_OPTIONS = [
  "dark",
  "light-gray",
  "white",
  "black",
  "hyperspace",
];

export const ICON_OPTIONS = [
  { value: "", label: "None (no icon)" },
  { value: "fieldmods", label: "Fieldmods warn" },
  { value: "globe", label: "Globe" },
  { value: "social-facebook", label: "Facebook" },
  { value: "social-linkedin", label: "Linkedin" },
  { value: "info", label: "Info" },
  { value: "loader", label: "Loader" },
  { value: "social-twitter", label: "Twitter" },
  { value: "social-youtube", label: "Youtube" },
  { value: "social-tiktok", label: "TikTok" },
  { value: "social-instagram", label: "Instagram" },
  { value: "map-pin", label: "Map with pin" },
  { value: "pin", label: "Pin (map)" },
  { value: "save", label: "Save" },
  { value: "times", label: "Times (cancel X)" },
  { value: "warn", label: "Warn" },
  { value: "megaphone", label: "Megaphone" },
  { value: "inventory", label: "Inventory" },
  { value: "program-tabs-labels", label: "Program Tabs Label" },
  { value: "link-left", label: "Link URL (left)" },
  { value: "link-right", label: "Link URL (right)" },
  { value: "send", label: "Send" },
  { value: "search", label: "Search" },
  { value: "download", label: "Download" },
  { value: "settings", label: "Settings" },
  { value: "show", label: "Show" },
  { value: "notifications", label: "Notifications" },
  { value: "maintenance", label: "Maintenance" },
  { value: "comment", label: "Comment" },
  { value: "home", label: "Home" },
  { value: "cart-buy-shop", label: "Cart Buy Shop" },
  { value: "us-dollars", label: "US Dollars" },
  { value: "error", label: "Error" },
  { value: "success", label: "Success" },
  { value: "location", label: "Location" },
  { value: "machine-iq", label: "Machine IQ" },
  { value: "bobcat", label: "Bobcat" },
  { value: "email", label: "Email" },
  { value: "print", label: "Print" },
  { value: "video", label: "Video" },
  { value: "phone", label: "Phone" },
  { value: "manual", label: "Manual" },
  { value: "parts", label: "Parts" },
  { value: "fault-code", label: "Fault Code" },
  { value: "user", label: "User" },
  { value: "3users", label: "3 users" },
  { value: "list", label: "List" },
  { value: "launch", label: "Launch" },
  { value: "attachments", label: "Attachments" },
  { value: "inventory-search", label: "Inventory Search" },
];

const ICON_VALUES = new Set(ICON_OPTIONS.map((opt) => opt.value));

const DEFAULT_BACKGROUND = "white";
const DEFAULT_TABLE_COLOR_SCHEME = "black";

function safeString(value, fallback = "") {
  return typeof value === "string" ? value : fallback;
}

function safeBool(value, fallback = false) {
  return typeof value === "boolean" ? value : fallback;
}

function safeNumber(value, fallback) {
  const n = Number(value);
  return Number.isFinite(n) ? n : fallback;
}

function nextId(prefix, existingIds) {
  let n = 1;
  while (existingIds.has(`${prefix}${n}`)) n++;
  const id = `${prefix}${n}`;
  existingIds.add(id);
  return id;
}

function uniqueId(prefix, providedId, existingIds) {
  if (providedId && !existingIds.has(providedId)) {
    existingIds.add(providedId);
    return providedId;
  }
  return nextId(prefix, existingIds);
}

function normalizeIcon(value) {
  const icon = safeString(value);
  if (!icon || icon === "none") return "";
  return ICON_VALUES.has(icon) ? icon : "";
}

function normalizeLink(link) {
  const type = LINK_TYPES.includes(link?.type) ? link.type : "none";
  return {
    type,
    pageLink: safeString(link?.pageLink),
    externalUrl: safeString(link?.externalUrl),
    target: LINK_TARGETS.includes(link?.target) ? link.target : "_self",
  };
}

function normalizeCell(cell, columnCount, existingIds) {
  const type = CELL_TYPES.includes(cell?.type) ? cell.type : "empty";
  return {
    id: uniqueId("c", safeString(cell?.id), existingIds),
    type,
    value: safeString(cell?.value),
    color: COLOR_OPTIONS.includes(cell?.color) ? cell.color : "default",
    bold: safeBool(cell?.bold),
    wrap: safeBool(cell?.wrap, true),
    link: normalizeLink(cell?.link),
  };
}

function normalizeRow(row, columnCount, existingIds, cellIds) {
  const cells = Array.isArray(row?.cells) ? row.cells.slice(0, columnCount) : [];
  while (cells.length < columnCount) {
    cells.push({});
  }
  return {
    id: uniqueId("r", safeString(row?.id), existingIds),
    label: safeString(row?.label),
    labelColor: COLOR_OPTIONS.includes(row?.labelColor)
      ? row.labelColor
      : "default",
    link: normalizeLink(row?.link),
    cells: cells.map((cell) => normalizeCell(cell, columnCount, cellIds)),
  };
}

function normalizeSection(section, columnCount, existingIds) {
  const rowIds = new Set();
  const cellIds = new Set();
  return {
    id: uniqueId("s", safeString(section?.id), existingIds),
    title: safeString(section?.title),
    rows: (Array.isArray(section?.rows) ? section.rows : []).map((row) =>
      normalizeRow(row, columnCount, rowIds, cellIds),
    ),
  };
}

function normalizeHeader(header, existingIds) {
  return {
    id: uniqueId("h", safeString(header?.id), existingIds),
    label: safeString(header?.label),
    description: safeString(header?.description),
    icon: normalizeIcon(header?.icon),
    image: safeString(header?.image),
    imageAlt: safeString(header?.imageAlt),
    link: normalizeLink(header?.link),
  };
}

export function createDefaultLayout() {
  return {
    background: DEFAULT_BACKGROUND,
    tableColorScheme: DEFAULT_TABLE_COLOR_SCHEME,
    title: "",
    titleAccent: "",
    subtitle: "",
    subtitleLink: { type: "none", pageLink: "", externalUrl: "", target: "_self" },
    headers: [
      { id: "h1", label: "BASE", description: "", icon: "", image: "", imageAlt: "", link: {} },
      { id: "h2", label: "COMFORT", description: "", icon: "", image: "", imageAlt: "", link: {} },
      { id: "h3", label: "COMFORT PLUS", description: "", icon: "", image: "", imageAlt: "", link: {} },
    ],
    sections: [
      {
        id: "s1",
        title: "STANDARD FEATURES",
        rows: [
          {
            id: "r1",
            label: "DELUXE DISPLAY",
            labelColor: "orange",
            cells: [{ type: "check" }, { type: "check" }, { type: "check" }],
          },
        ],
      },
    ],
    showMore: false,
    showMoreRowLimit: 5,
    disclaimers: ["", "", ""],
  };
}

export function parseLayout(value, fallbackFactory) {
  if (!value) return fallbackFactory();
  try {
    const parsed = JSON.parse(value);
    if (parsed && typeof parsed === "object") return parsed;
  } catch {
    // fall through
  }
  return fallbackFactory();
}

export function normalizeLayout(layout) {
  const fallback = createDefaultLayout();
  const source = layout && typeof layout === "object" ? layout : fallback;

  const headerIds = new Set();
  const headers = (
    Array.isArray(source.headers) && source.headers.length
      ? source.headers
      : fallback.headers
  ).map((header) => normalizeHeader(header, headerIds));
  const columnCount = headers.length;

  const sectionIds = new Set();
  const sections = (
    Array.isArray(source.sections) && source.sections.length
      ? source.sections
      : fallback.sections
  ).map((section) => normalizeSection(section, columnCount, sectionIds));

  const disclaimers = Array.isArray(source.disclaimers)
    ? source.disclaimers.slice(0, 3).map((d) => safeString(d))
    : ["", "", ""];
  while (disclaimers.length < 3) disclaimers.push("");

  return {
    background: BACKGROUND_OPTIONS.includes(source.background)
      ? source.background
      : DEFAULT_BACKGROUND,
    tableColorScheme: TABLE_COLOR_SCHEMES.includes(source.tableColorScheme)
      ? source.tableColorScheme
      : DEFAULT_TABLE_COLOR_SCHEME,
    title: safeString(source.title),
    titleAccent: safeString(source.titleAccent),
    subtitle: safeString(source.subtitle),
    subtitleLink: normalizeLink(source.subtitleLink),
    headers,
    sections,
    showMore: safeBool(source.showMore),
    showMoreRowLimit: Math.max(1, safeNumber(source.showMoreRowLimit, 5)),
    disclaimers,
  };
}

export function createEmptyHeader(existingHeaders) {
  const ids = new Set(existingHeaders.map((h) => h.id));
  return normalizeHeader(
    {
      label: "New column",
      description: "",
      icon: "",
      image: "",
      imageAlt: "",
    },
    ids,
  );
}

export function createEmptyRow(
  columnCount,
  existingRowIds = new Set(),
  existingCellIds = new Set(),
) {
  return normalizeRow(
    {
      label: "",
      cells: Array.from({ length: columnCount }, () => ({ type: "empty" })),
    },
    columnCount,
    existingRowIds,
    existingCellIds,
  );
}

export function createEmptyCell(existingCellIds = new Set()) {
  return normalizeCell({ type: "empty" }, 1, existingCellIds);
}

export function createEmptySection(columnCount, existingSections) {
  const ids = new Set(existingSections.map((s) => s.id));
  return normalizeSection(
    {
      title: "New section",
      rows: [
        {
          label: "",
          cells: Array.from({ length: columnCount }, () => ({ type: "empty" })),
        },
      ],
    },
    columnCount,
    ids,
  );
}
