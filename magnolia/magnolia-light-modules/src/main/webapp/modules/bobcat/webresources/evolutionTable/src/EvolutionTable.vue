<template>
  <div class="et" @keydown="stopVaadinCapture" @keyup="stopVaadinCapture">
    <div v-show="!editorModalOpen" class="et__toolbar">
      <div class="et__toolbar-group">
        <label class="et__label">Background</label>
        <select v-model="layout.background" class="et__input" @change="emitChange">
          <option v-for="opt in BACKGROUND_OPTIONS" :key="opt" :value="opt">
            {{ backgroundLabel(opt) }}
          </option>
        </select>
      </div>
      <div class="et__toolbar-group">
        <label class="et__label">Table colour</label>
        <select v-model="layout.tableColorScheme" class="et__input" @change="emitChange">
          <option value="black">Black top heading + grey left column</option>
          <option value="white">White top heading + grey left column</option>
        </select>
      </div>
      <div class="et__toolbar-group">
        <label class="et__label">
          <input type="checkbox" v-model="layout.showMore" @change="emitChange" />
          Enable "Show more"
        </label>
        <input
          v-if="layout.showMore"
          type="number"
          min="1"
          class="et__input et__input--small"
          v-model.number="layout.showMoreRowLimit"
          title="Number of rows visible before Show more"
          @change="changeShowMoreRowLimit"
        />
      </div>
    </div>

    <div v-show="!editorModalOpen" class="et__title-row">
      <label class="et__label">Title</label>
      <input
        type="text"
        class="et__input et__input--title"
        v-model="layout.title"
        placeholder="T680"
        @input="emitChange"
      />
      <input
        type="text"
        class="et__input et__input--title et__input--accent"
        v-model="layout.titleAccent"
        placeholder="PACKAGES"
        @input="emitChange"
      />
    </div>

    <div v-show="!editorModalOpen" class="et__subtitle-row">
      <label class="et__label">Subtitle</label>
      <input
        type="text"
        class="et__input et__input--accent"
        v-model="layout.subtitle"
        placeholder="HOW TO CHOOSE THE BEST LOADER OPTIONS"
        @input="emitChange"
      />
      <button
        type="button"
        class="et__icon-btn"
        :class="{ 'et__icon-btn--active': layout.subtitleLink.type !== 'none' }"
        title="Edit subtitle link"
        @click="editSubtitleLink"
      >
        🔗
      </button>
    </div>

    <div v-show="!editorModalOpen" class="et__table-wrap" :class="['et__table-wrap--' + layout.tableColorScheme]">
      <table class="et__table">
        <thead>
          <tr class="et__header-row">
            <th class="et__header-cell et__header-cell--label">
              <span class="et__header-tag">Row label column</span>
            </th>
            <th
              v-for="(header, hIdx) in layout.headers"
              :key="header.id"
              class="et__header-cell"
            >
              <div class="et__header-content">
                <input
                  type="text"
                  class="et__input et__input--header"
                  v-model="header.label"
                  placeholder="Column title"
                  @input="emitChange"
                />
                <textarea
                  class="et__input et__textarea"
                  v-model="header.description"
                  placeholder="Optional description shown below title"
                  @input="emitChange"
                ></textarea>
                <div class="et__header-icon-field">
                  <label class="et__label">Icon (optional)</label>
                  <select
                    class="et__input et__input--small"
                    v-model="header.icon"
                    @change="emitChange"
                  >
                    <option v-for="opt in ICON_OPTIONS" :key="opt.value" :value="opt.value">
                      {{ opt.label }}
                    </option>
                  </select>
                </div>
                <div class="et__header-image">
                  <input
                    type="text"
                    class="et__input et__input--small"
                    v-model="header.image"
                    placeholder="Optional graphic URL"
                    @input="emitChange"
                  />
                  <input
                    v-if="header.image"
                    type="text"
                    class="et__input et__input--small"
                    v-model="header.imageAlt"
                    placeholder="Alt text"
                    @input="emitChange"
                  />
                  <img
                    v-if="header.image"
                    :src="header.image"
                    :alt="header.imageAlt"
                    class="et__header-image-preview"
                  />
                </div>
                <div class="et__header-controls">
                  <button
                    type="button"
                    class="et__icon-btn"
                    title="Move column left"
                    :disabled="hIdx === 0"
                    @click="moveColumn(hIdx, -1)"
                  >
                    ◀
                  </button>
                  <button
                    type="button"
                    class="et__icon-btn"
                    title="Move column right"
                    :disabled="hIdx === layout.headers.length - 1"
                    @click="moveColumn(hIdx, 1)"
                  >
                    ▶
                  </button>
                  <button
                    type="button"
                    class="et__icon-btn"
                    title="Link options"
                    @click="editHeaderLink(hIdx)"
                  >
                    <span :class="header.link.type !== 'none' ? 'et__icon-btn--active' : ''">🔗</span>
                  </button>
                  <button
                    type="button"
                    class="et__icon-btn et__icon-btn--danger"
                    title="Remove column"
                    :disabled="layout.headers.length <= 1"
                    @click="removeColumn(hIdx)"
                  >
                    ✕
                  </button>
                </div>
              </div>
            </th>
            <th class="et__header-cell et__header-cell--actions">
              <button
                type="button"
                class="et__btn et__btn--add"
                @click="addColumn"
              >
                + Column
              </button>
            </th>
          </tr>
        </thead>

        <tbody
          v-for="(section, sIdx) in layout.sections"
          :key="section.id"
          class="et__section"
        >
          <tr class="et__section-row">
            <td class="et__section-cell" :colspan="layout.headers.length + 1">
              <input
                type="text"
                class="et__input et__input--section"
                v-model="section.title"
                placeholder="Section title (e.g. STANDARD FEATURES)"
                @input="emitChange"
              />
            </td>
            <td class="et__section-actions">
              <div class="et__section-action-buttons">
                <button
                  type="button"
                  class="et__icon-btn"
                  title="Move section up"
                  :disabled="sIdx === 0"
                  @click="moveSection(sIdx, -1)"
                >
                  ▲
                </button>
                <button
                  type="button"
                  class="et__icon-btn"
                  title="Move section down"
                  :disabled="sIdx === layout.sections.length - 1"
                  @click="moveSection(sIdx, 1)"
                >
                  ▼
                </button>
                <button
                  type="button"
                  class="et__icon-btn et__icon-btn--danger"
                  title="Remove section"
                  :disabled="layout.sections.length <= 1"
                  @click="removeSection(sIdx)"
                >
                  ✕
                </button>
              </div>
            </td>
          </tr>

          <tr
            v-for="(row, rIdx) in section.rows"
            :key="row.id"
            class="et__row"
          >
            <td class="et__row-label">
              <input
                type="text"
                class="et__input"
                v-model="row.label"
                placeholder="Row label"
                @input="emitChange"
              />
              <div class="et__row-label-controls">
                <select
                  v-model="row.labelColor"
                  class="et__input et__input--small"
                  title="Label color"
                  @change="emitChange"
                >
                  <option v-for="c in COLOR_OPTIONS" :key="c" :value="c">
                    {{ c }}
                  </option>
                </select>
                <button
                  type="button"
                  class="et__icon-btn"
                  :class="{ 'et__icon-btn--active': row.link.type !== 'none' }"
                  title="Link options"
                  @click="editRowLink(sIdx, rIdx)"
                >
                  🔗
                </button>
              </div>
            </td>
            <td
              v-for="(cell, cIdx) in row.cells"
              :key="cell.id"
              class="et__cell"
              :class="cellClasses(cell)"
            >
              <div class="et__cell-content">
                <select
                  class="et__input et__input--small et__cell-type"
                  v-model="cell.type"
                  @change="emitChange"
                >
                  <option value="empty">—</option>
                  <option value="check">✓ Check</option>
                  <option value="dash">- Dash</option>
                  <option value="text">Text</option>
                </select>
                <input
                  v-if="cell.type === 'text'"
                  type="text"
                  class="et__input et__cell-input"
                  v-model="cell.value"
                  placeholder="Cell text"
                  @input="emitChange"
                />
              </div>
              <div v-if="cell.type === 'text'" class="et__cell-tools">
                <button
                  type="button"
                  class="et__icon-btn"
                  :class="{ 'et__icon-btn--active': cell.link.type !== 'none' || cell.color !== 'default' }"
                  title="Style and link"
                  @click="editCell(sIdx, rIdx, cIdx)"
                >
                  ⚙
                </button>
              </div>
            </td>
            <td class="et__row-actions">
              <button
                type="button"
                class="et__icon-btn"
                title="Move row up"
                :disabled="rIdx === 0"
                @click="moveRow(sIdx, rIdx, -1)"
              >
                ▲
              </button>
              <button
                type="button"
                class="et__icon-btn"
                title="Move row down"
                :disabled="rIdx === section.rows.length - 1"
                @click="moveRow(sIdx, rIdx, 1)"
              >
                ▼
              </button>
              <button
                type="button"
                class="et__icon-btn et__icon-btn--danger"
                title="Remove row"
                :disabled="section.rows.length <= 1"
                @click="removeRow(sIdx, rIdx)"
              >
                ✕
              </button>
            </td>
          </tr>

          <tr class="et__row-add">
            <td :colspan="layout.headers.length + 2">
              <button
                type="button"
                class="et__btn et__btn--add"
                @click="addRow(sIdx)"
              >
                + Row in "{{ section.title || 'this section' }}"
              </button>
            </td>
          </tr>
        </tbody>

        <tfoot>
          <tr>
            <td :colspan="layout.headers.length + 2" class="et__section-add">
              <button
                type="button"
                class="et__btn et__btn--add"
                @click="addSection"
              >
                + Add section
              </button>
            </td>
          </tr>
        </tfoot>
      </table>
    </div>

    <div v-show="!editorModalOpen" class="et__disclaimers">
      <label class="et__label">Disclaimers (up to 3)</label>
      <div
        v-for="(disclaimer, idx) in layout.disclaimers"
        :key="`disc-${idx}`"
        class="et__disclaimer-row"
      >
        <span class="et__disclaimer-index">{{ idx + 1 }}</span>
        <input
          type="text"
          class="et__input"
          :value="disclaimer"
          :placeholder="`Disclaimer ${idx + 1}`"
          @input="updateDisclaimer(idx, $event.target.value)"
        />
      </div>
    </div>

    <div v-if="editorModalOpen" class="et__editor">
        <div class="et__modal">
          <div class="et__modal-header">
            <span class="et__modal-title">{{ modalTitle }}</span>
            <button type="button" class="et__modal-close" @click="closeEditor">×</button>
          </div>
          <div class="et__modal-body">
            <template v-if="editorContext?.kind === 'cell'">
              <div class="et__field">
                <label class="et__label">Cell type</label>
                <select v-model="editorDraft.type" class="et__input">
                  <option value="empty">Empty</option>
                  <option value="check">Checkmark</option>
                  <option value="dash">Dash</option>
                  <option value="text">Text</option>
                </select>
              </div>
              <div v-if="editorDraft.type === 'text'" class="et__field">
                <label class="et__label">Text</label>
                <input type="text" class="et__input" v-model="editorDraft.value" />
              </div>
              <div v-if="editorDraft.type === 'text'" class="et__field">
                <label class="et__label">Colour</label>
                <select v-model="editorDraft.color" class="et__input">
                  <option v-for="c in COLOR_OPTIONS" :key="c" :value="c">{{ c }}</option>
                </select>
              </div>
              <div v-if="editorDraft.type === 'text'" class="et__field">
                <label class="et__label">
                  <input type="checkbox" v-model="editorDraft.bold" />
                  Bold
                </label>
                <label class="et__label">
                  <input type="checkbox" v-model="editorDraft.wrap" />
                  Allow text wrap
                </label>
              </div>
            </template>

            <div class="et__field">
              <label class="et__label">Link type</label>
              <select v-model="editorDraft.link.type" class="et__input">
                <option value="none">No link</option>
                <option value="page">Internal page link</option>
                <option value="external">External URL</option>
              </select>
            </div>
            <div v-if="editorDraft.link.type === 'page'" class="et__field">
              <label class="et__label">Page path (e.g. /products/loader)</label>
              <input
                type="text"
                class="et__input"
                v-model="editorDraft.link.pageLink"
              />
            </div>
            <div v-if="editorDraft.link.type === 'external'" class="et__field">
              <label class="et__label">External URL</label>
              <input
                type="text"
                class="et__input"
                v-model="editorDraft.link.externalUrl"
              />
            </div>
            <div v-if="editorDraft.link.type !== 'none'" class="et__field">
              <label class="et__label">Link target</label>
              <select v-model="editorDraft.link.target" class="et__input">
                <option value="_self">Same window</option>
                <option value="_blank">New window</option>
              </select>
            </div>
          </div>
          <div class="et__modal-footer">
            <button type="button" class="et__btn" @click="closeEditor">Cancel</button>
            <button type="button" class="et__btn et__btn--save" @click="saveEditor">
              Save
            </button>
          </div>
        </div>
    </div>

    <details v-show="!editorModalOpen" class="et__preview">
      <summary class="et__preview-label">JSON output</summary>
      <pre class="et__json">{{ jsonPreview }}</pre>
    </details>
  </div>
</template>

<script setup>
import { computed, reactive, watch } from "vue";
import {
  BACKGROUND_OPTIONS,
  COLOR_OPTIONS,
  ICON_OPTIONS,
  createDefaultLayout,
  createEmptyCell,
  createEmptyHeader,
  createEmptyRow,
  createEmptySection,
  normalizeLayout,
  parseLayout,
} from "./utils/evolutionTable.js";
import { useCellEditor } from "./composables/useCellEditor.js";

const props = defineProps({
  value: { type: String, default: "" },
});
const emit = defineEmits(["change"]);

const layout = reactive(
  normalizeLayout(parseLayout(props.value, createDefaultLayout)),
);

let localChangeInFlight = false;

function snapshot() {
  return JSON.parse(JSON.stringify(layout));
}

function applyState(next) {
  Object.keys(layout).forEach((key) => {
    if (!(key in next)) delete layout[key];
  });
  Object.assign(layout, next);
}

function emitChange() {
  localChangeInFlight = true;
  emit("change", JSON.stringify(snapshot()));
}

function changeShowMoreRowLimit() {
  const limit = Math.floor(Number(layout.showMoreRowLimit));
  layout.showMoreRowLimit = Number.isFinite(limit) && limit > 0 ? limit : 5;
  emitChange();
}

const jsonPreview = computed(() => JSON.stringify(snapshot(), null, 2));

watch(
  () => props.value,
  (next) => {
    if (localChangeInFlight) {
      localChangeInFlight = false;
      return;
    }
    applyState(normalizeLayout(parseLayout(next, createDefaultLayout)));
  },
);

function stopVaadinCapture(event) {
  event.stopPropagation();
}

function backgroundLabel(value) {
  switch (value) {
    case "dark":
      return "Dark";
    case "light-gray":
      return "Light gray";
    case "white":
      return "White";
    case "black":
      return "Black";
    case "hyperspace":
      return "Hyperspace";
    default:
      return value;
  }
}

function cellClasses(cell) {
  const classes = [];
  if (cell.type !== "empty") classes.push(`et__cell--${cell.type}`);
  if (cell.color && cell.color !== "default") {
    classes.push(`et__cell--color-${cell.color}`);
  }
  return classes;
}

function addColumn() {
  layout.headers.push(createEmptyHeader(layout.headers));
  layout.sections.forEach((section) => {
    const cellIds = new Set();
    section.rows.forEach((row) => row.cells.forEach((c) => cellIds.add(c.id)));
    section.rows.forEach((row) => {
      row.cells.push(createEmptyCell(cellIds));
    });
  });
  emitChange();
}

function removeColumn(index) {
  if (layout.headers.length <= 1) return;
  layout.headers.splice(index, 1);
  layout.sections.forEach((section) => {
    section.rows.forEach((row) => row.cells.splice(index, 1));
  });
  emitChange();
}

function moveColumn(index, delta) {
  const target = index + delta;
  if (target < 0 || target >= layout.headers.length) return;
  const [header] = layout.headers.splice(index, 1);
  layout.headers.splice(target, 0, header);
  layout.sections.forEach((section) => {
    section.rows.forEach((row) => {
      const [cell] = row.cells.splice(index, 1);
      row.cells.splice(target, 0, cell);
    });
  });
  emitChange();
}

function moveSection(index, delta) {
  const target = index + delta;
  if (target < 0 || target >= layout.sections.length) return;
  const [section] = layout.sections.splice(index, 1);
  layout.sections.splice(target, 0, section);
  emitChange();
}

function addSection() {
  layout.sections.push(createEmptySection(layout.headers.length, layout.sections));
  emitChange();
}

function removeSection(index) {
  if (layout.sections.length <= 1) return;
  layout.sections.splice(index, 1);
  emitChange();
}

function addRow(sectionIndex) {
  const section = layout.sections[sectionIndex];
  const rowIds = new Set(section.rows.map((r) => r.id));
  const cellIds = new Set();
  section.rows.forEach((r) => r.cells.forEach((c) => cellIds.add(c.id)));
  section.rows.push(createEmptyRow(layout.headers.length, rowIds, cellIds));
  emitChange();
}

function removeRow(sectionIndex, rowIndex) {
  const rows = layout.sections[sectionIndex].rows;
  if (rows.length <= 1) return;
  rows.splice(rowIndex, 1);
  emitChange();
}

function moveRow(sectionIndex, rowIndex, delta) {
  const rows = layout.sections[sectionIndex].rows;
  const target = rowIndex + delta;
  if (target < 0 || target >= rows.length) return;
  const [row] = rows.splice(rowIndex, 1);
  rows.splice(target, 0, row);
  emitChange();
}

function updateDisclaimer(index, value) {
  layout.disclaimers[index] = value;
  emitChange();
}

const {
  modalOpen: editorModalOpen,
  context: editorContext,
  draft: editorDraft,
  open: openEditor,
  close: closeEditor,
  save: saveEditor,
} = useCellEditor((ctx, draft) => {
  if (ctx.kind === "cell") {
    const target = layout.sections[ctx.sectionIndex].rows[ctx.rowIndex].cells[ctx.cellIndex];
    target.type = draft.type;
    target.value = draft.value;
    target.color = draft.color;
    target.bold = draft.bold;
    target.wrap = draft.wrap;
    target.link = draft.link;
  } else if (ctx.kind === "row") {
    layout.sections[ctx.sectionIndex].rows[ctx.rowIndex].link = draft.link;
  } else if (ctx.kind === "header") {
    layout.headers[ctx.headerIndex].link = draft.link;
  } else if (ctx.kind === "subtitle") {
    layout.subtitleLink = draft.link;
  }
  emitChange();
});

function editCell(sectionIndex, rowIndex, cellIndex) {
  const cell = layout.sections[sectionIndex].rows[rowIndex].cells[cellIndex];
  openEditor({ kind: "cell", sectionIndex, rowIndex, cellIndex }, cell);
}

function editRowLink(sectionIndex, rowIndex) {
  const row = layout.sections[sectionIndex].rows[rowIndex];
  openEditor(
    { kind: "row", sectionIndex, rowIndex },
    { type: "text", value: row.label, color: row.labelColor, link: row.link },
  );
}

function editHeaderLink(headerIndex) {
  const header = layout.headers[headerIndex];
  openEditor(
    { kind: "header", headerIndex },
    { type: "text", value: header.label, link: header.link },
  );
}

function editSubtitleLink() {
  openEditor(
    { kind: "subtitle" },
    { type: "text", value: layout.subtitle, link: layout.subtitleLink },
  );
}

const modalTitle = computed(() => {
  if (!editorContext.value) return "Edit";
  switch (editorContext.value.kind) {
    case "cell":
      return "Edit cell";
    case "row":
      return "Edit row link";
    case "header":
      return "Edit column link";
    case "subtitle":
      return "Edit subtitle link";
    default:
      return "Edit";
  }
});
</script>

<style>
*,
*::before,
*::after {
  box-sizing: border-box;
}
.et {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  font-size: 13px;
  color: #2a2f36;
  padding: 4px;
}
.et__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 12px;
  background: #f7f8fa;
  border: 1px solid #e0e3e8;
  border-radius: 6px;
  margin-bottom: 14px;
}
.et__toolbar-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 180px;
}
.et__label {
  font-size: 11px;
  font-weight: 600;
  color: #4a5060;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  display: flex;
  align-items: center;
  gap: 6px;
}
.et__input {
  border: 1px solid #c9cfd6;
  border-radius: 4px;
  padding: 6px 8px;
  background: #fff;
  font-size: 13px;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.et__input:focus {
  border-color: #4a90d9;
  box-shadow: 0 0 0 2px rgba(74, 144, 217, 0.18);
}
.et__input--small {
  padding: 4px 6px;
  font-size: 12px;
}
.et__input--title {
  font-weight: 700;
  font-size: 18px;
  flex: 1;
}
.et__input--accent {
  color: #ff3600;
}
.et__title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.et__subtitle-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.et__subtitle-row .et__input {
  flex: 1;
  font-weight: 700;
  text-transform: uppercase;
}
.et__table-wrap {
  border: 1px solid #e0e3e8;
  border-radius: 6px;
  overflow: auto;
  background: #fff;
}
.et__table {
  width: 100%;
  border-collapse: collapse;
}
.et__header-row {
  background: #212529;
  color: #fff;
}
.et__table-wrap--white .et__header-row {
  background: #ffffff;
  color: #212529;
  border-bottom: 2px solid #ced4da;
}
.et__header-cell {
  padding: 12px;
  vertical-align: top;
  text-align: left;
  border-right: 1px solid #2f343b;
}
.et__table-wrap--white .et__header-cell {
  border-right-color: #e9ecef;
}
.et__header-cell--label {
  width: 220px;
  background: #f1f3f5;
  color: #2a2f36;
}
.et__header-cell--actions {
  width: 90px;
  text-align: center;
}
.et__header-tag {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #6c757d;
}
.et__header-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.et__input--header {
  font-weight: 700;
  text-transform: uppercase;
  background: rgba(255, 255, 255, 0.85);
  color: #212529;
}
.et__table-wrap--white .et__input--header {
  background: #fff;
}
.et__textarea {
  min-height: 44px;
  resize: vertical;
  background: rgba(255, 255, 255, 0.85);
  color: #212529;
}
.et__header-controls {
  display: flex;
  gap: 4px;
  justify-content: flex-end;
}
.et__header-icon-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.et__header-image {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 6px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px dashed rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}
.et__table-wrap--white .et__header-image {
  background: #f8f9fa;
  border-color: #ced4da;
}
.et__header-image-preview {
  max-height: 60px;
  max-width: 100%;
  object-fit: contain;
  background: #fff;
  border-radius: 4px;
  padding: 4px;
}
.et__icon-btn {
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.15);
  color: inherit;
  border-radius: 4px;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 13px;
  line-height: 1;
  transition: all 0.15s;
}
.et__icon-btn:hover:not(:disabled) {
  background: rgba(74, 144, 217, 0.18);
  border-color: rgba(74, 144, 217, 0.45);
}
.et__icon-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}
.et__icon-btn--active {
  color: #ff3600;
  font-weight: 700;
}
.et__icon-btn--danger:hover:not(:disabled) {
  background: #fdecea;
  border-color: #f5c2c0;
  color: #c0392b;
}
.et__section-row td {
  background: #adb5bd;
  color: #fff;
  padding: 6px 10px;
}
.et__input--section {
  background: transparent;
  border: 1px dashed rgba(255, 255, 255, 0.5);
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  width: 100%;
}
.et__input--section:focus {
  background: rgba(255, 255, 255, 0.15);
  border-color: #fff;
}
.et__input--section::placeholder {
  color: rgba(255, 255, 255, 0.7);
}
.et__row {
  border-bottom: 1px solid #e9ecef;
}
.et__row:nth-child(odd) td.et__cell {
  background: #fafbfc;
}
.et__row-label {
  background: #f1f3f5;
  padding: 10px 12px;
  vertical-align: middle;
  border-right: 1px solid #e0e3e8;
}
.et__row-label-controls {
  display: flex;
  gap: 4px;
  margin-top: 4px;
  align-items: center;
}
.et__cell {
  padding: 10px;
  border-right: 1px solid #e9ecef;
  vertical-align: middle;
}
.et__cell-content {
  display: flex;
  gap: 6px;
  align-items: center;
}
.et__cell-type {
  flex-shrink: 0;
  width: 96px;
}
.et__cell-input {
  flex: 1;
  min-width: 80px;
}
.et__cell-tools {
  margin-top: 4px;
}
.et__cell--color-orange .et__cell-input {
  color: #ff3600;
  font-weight: 700;
}
.et__cell--color-red .et__cell-input {
  color: #c0392b;
}
.et__cell--check {
  background-color: rgba(10, 166, 86, 0.06);
}
.et__row-actions {
  padding: 6px;
  text-align: center;
  width: 110px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}
.et__section-cell {
  padding: 8px 10px;
}
.et__section-actions {
  width: 210px;
  background: #adb5bd;
  white-space: nowrap;
  padding: 4px 6px;
  text-align: center;
  vertical-align: middle;
}
.et__section-actions .et__icon-btn {
  color: #fff;
}
.et__section-action-buttons {
  display: flex;
  gap: 4px;
  justify-content: flex-end;
  margin-top: 4px;
}
.et__row-add td {
  padding: 6px 10px;
  background: #f8f9fa;
  text-align: center;
}
.et__section-add {
  padding: 8px;
  text-align: center;
  background: #f1f3f5;
}
.et__btn {
  padding: 6px 14px;
  border-radius: 4px;
  border: 1px solid #c9cfd6;
  background: #fff;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.15s;
  color: #2a2f36;
}
.et__btn:hover {
  background: #f0f0f0;
}
.et__btn--add {
  border-color: #019c68;
  color: #019c68;
}
.et__btn--add:hover {
  background: #e6f6ee;
}
.et__btn--save {
  border-color: #4a90d9;
  background: #4a90d9;
  color: #fff;
}
.et__btn--save:hover {
  background: #3a7cc6;
}
.et__disclaimers {
  margin-top: 18px;
  padding: 12px;
  background: #f7f8fa;
  border: 1px solid #e0e3e8;
  border-radius: 6px;
}
.et__disclaimer-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}
.et__disclaimer-row .et__input {
  flex: 1;
}
.et__disclaimer-index {
  font-size: 11px;
  font-weight: 700;
  color: #6c757d;
  width: 16px;
  text-align: center;
}
.et__editor {
  display: flex;
  justify-content: center;
  width: 100%;
  padding: 4px 0 8px;
}
.et__modal {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 760px;
  overflow: hidden;
}
.et__modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid #e9ecef;
}
.et__modal-title {
  font-size: 15px;
  font-weight: 600;
}
.et__modal-close {
  border: none;
  background: transparent;
  font-size: 22px;
  cursor: pointer;
  color: #888;
  padding: 0 4px;
  line-height: 1;
}
.et__modal-body {
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.et__field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.et__modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 18px;
  border-top: 1px solid #e9ecef;
  background: #fafbfc;
}
.et__preview {
  margin-top: 14px;
  border: 1px solid #e0e3e8;
  border-radius: 4px;
  overflow: hidden;
}
.et__preview-label {
  padding: 6px 12px;
  background: #f1f3f5;
  cursor: pointer;
  font-size: 11px;
  color: #555;
  user-select: none;
}
.et__json {
  margin: 0;
  padding: 12px;
  font-size: 11px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  background: #1e1e1e;
  color: #9cdcfe;
  overflow-x: auto;
  white-space: pre;
}
</style>
