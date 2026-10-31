<template>
  <div class="bl" @keydown="stopVaadinCapture" @keyup="stopVaadinCapture">
    <div v-show="!editModalOpen" class="bl__toolbar">
      <button
        type="button"
        class="bl__btn bl__btn--add bl__btn--add-tile"
        :disabled="!canAddTile"
        :title="canAddTile ? 'Add a new tile' : `Maximum ${TILE_LIMIT} tiles reached`"
        @click="addTile"
      >
        + Add Tile
      </button>

      <select
        v-model="activeDevice"
        class="bl__device-select bl__device-select--toolbar"
      >
        <option value="mobile">Mobile</option>
        <option value="tablet">Tablet</option>
        <option value="desktop">Desktop</option>
      </select>

      <button
        type="button"
        class="bl__btn bl__btn--add"
        :class="{ 'bl__btn--view-active': activeView === '16' }"
        @click="setView16"
      >
        Set view for 16 items
      </button>

      <button
        type="button"
        class="bl__btn bl__btn--add"
        :class="{ 'bl__btn--view-active': activeView === '11' }"
        @click="setView11"
      >
        Set view for 11 items
      </button>
    </div>

    <div v-show="!editModalOpen" ref="gridEl" class="grid-stack bl__grid"></div>

    <div v-if="editModalOpen" class="bl__editor" :key="activeDevice">
        <div class="bl__modal">
          <div class="bl__modal-header">
            <span class="bl__modal-title">Edit tile content</span>
            <button type="button" class="bl__modal-close" @click="cancelEdit">
              &times;
            </button>
          </div>
          <div class="bl__modal-body">
            <label class="bl__modal-label" for="bl-edit-before"
              >Upper title</label
            >
            <QuillEditor
              id="bl-edit-before"
              v-model:content="editBefore"
              content-type="html"
              theme="snow"
              :options="quillOptions"
              class="bl__quill"
            />

            <div class="bl__modal-section">
              <label class="bl__modal-label" for="bl-edit-after"
                >Sub title</label
              >
              <QuillEditor
                id="bl-edit-after"
                v-model:content="editAfter"
                content-type="html"
                theme="snow"
                :options="quillOptions"
                class="bl__quill"
              />
            </div>

            <div class="bl__modal-section">
              <label class="bl__modal-label" for="bl-edit-bg-color"
                >Background Color (optional)</label
              >
              <select
                id="bl-edit-bg-color"
                v-model="editBgColor"
                class="bl__modal-input"
              >
                <option value="none">None</option>
                <option value="black">Black</option>
                <option value="white">White</option>
                <option value="gray-lighter">Light gray</option>
              </select>
            </div>

            <div class="bl__modal-section">
              <label class="bl__modal-label" for="bl-edit-tile-option"
                >Option Selection for the tile/element</label
              >
              <select
                id="bl-edit-tile-option"
                v-model="editTileOption"
                class="bl__modal-input"
              >
                <option value="img">Image</option>
                <option value="imgBg">Background Image</option>
                <option value="title">Title</option>
              </select>
            </div>

            <div v-if="editTileOption === 'title'" class="bl__modal-section">
              <label class="bl__modal-label" for="bl-edit-content-title"
                >Text</label
              >
              <QuillEditor
                id="bl-edit-content-title"
                v-model:content="editContentTitle"
                content-type="html"
                theme="snow"
                :options="quillOptions"
                class="bl__quill"
              />
            </div>

            <div v-if="editTileOption === 'img'" class="bl__modal-section">
              <label class="bl__modal-label">Image</label>
              <div class="bl__modal-image-row">
                <template v-if="editImg">
                  <img
                    :src="editImg"
                    class="bl__modal-img-preview"
                    alt="Tile Image"
                  />
                  <span class="bl__modal-img-url" :title="editImg">{{
                    editImg
                  }}</span>
                  <button
                    type="button"
                    class="bl__btn bl__btn--remove-img"
                    @click="editImg = ''"
                  >
                    Remove
                  </button>
                </template>
                <button
                  v-else
                  type="button"
                  class="bl__btn bl__btn--cloudinary"
                  :disabled="cloudinaryImgLoading"
                  @click="openCloudinaryImgPicker"
                >
                  <svg
                    viewBox="0 0 24 24"
                    width="16"
                    height="16"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="2" />
                    <circle cx="8.5" cy="8.5" r="1.5" />
                    <polyline points="21 15 16 10 5 21" />
                  </svg>
                  {{ cloudinaryImgLoading ? "Loading..." : "Add Image" }}
                </button>
              </div>
            </div>

            <div v-if="editTileOption === 'imgBg'" class="bl__modal-section">
              <label class="bl__modal-label">Background Image</label>
              <div class="bl__modal-image-row">
                <template v-if="editImBg">
                  <img
                    :src="editImBg"
                    class="bl__modal-img-preview"
                    alt="Tile Background Image"
                  />
                  <span class="bl__modal-img-url" :title="editImBg">{{
                    editImBg
                  }}</span>
                  <button
                    type="button"
                    class="bl__btn bl__btn--remove-img"
                    @click="editImBg = ''"
                  >
                    Remove
                  </button>
                </template>
                <button
                  v-else
                  type="button"
                  class="bl__btn bl__btn--cloudinary"
                  :disabled="cloudinaryImBgLoading"
                  @click="openCloudinaryImBgPicker"
                >
                  <svg
                    viewBox="0 0 24 24"
                    width="16"
                    height="16"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="2" />
                    <circle cx="8.5" cy="8.5" r="1.5" />
                    <polyline points="21 15 16 10 5 21" />
                  </svg>
                  {{ cloudinaryImBgLoading ? "Loading..." : "Add Image" }}
                </button>
              </div>
            </div>
          </div>
          <div class="bl__modal-footer">
            <button type="button" class="bl__btn" @click="cancelEdit">
              Cancel
            </button>
            <button
              type="button"
              class="bl__btn bl__btn--save"
              @click="saveEditContent"
            >
              Save
            </button>
          </div>
        </div>
    </div>

    <details v-if="isDev" class="bl__preview">
      <summary class="bl__preview-label">JSON output</summary>
      <pre class="bl__json">{{ jsonPreview }}</pre>
    </details>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { QuillEditor } from "@vueup/vue-quill";
import "@vueup/vue-quill/dist/vue-quill.snow.css";
import { injectBentoStyles } from "./composables/useBentoStyles";
import { useGridStackLayout } from "./composables/useGridStackLayout";
import { useTileEditor } from "./composables/useTileEditor";
import { DEVICES } from "./utils/bentoWidget";
import {
  createDefaultLayout,
  createDefaultLayout16,
} from "./constants/defaultLayout";

const props = defineProps({
  value: {
    type: String,
    default: "",
  },
  cloudName: {
    type: String,
    default: "",
  },
  apiKey: {
    type: String,
    default: "",
  },
});

const isDev = import.meta.env.DEV;

const emit = defineEmits(["change"]);
const activeDevice = ref("desktop");
const devices = DEVICES;
const deviceLabels = {
  mobile: "Mobile",
  tablet: "Tablet",
  desktop: "Desktop",
};
const quillOptions = {
  modules: {
    toolbar: [
      ["bold", "italic", "underline"],
      [{ header: [1, 2, 3, 4, 5, 6, false] }],
      [
        {
          color: [
            "#ff3600",
            "#fd7e14",
            "#0AA656",
            "#333333",
            "#c4c4c4",
            "#fff",
            "#000",
            "#777777",
            "#f1f1f1",
          ],
        },
        {
          background: [
            "#ff3600",
            "#fd7e14",
            "#0AA656",
            "#333333",
            "#c4c4c4",
            "#fff",
            "#000",
            "#777777",
            "#f1f1f1",
          ],
        },
      ],
      [{ list: "ordered" }, { list: "bullet" }],
      ["link"],
      ["clean"],
    ],
  },
  placeholder: "Enter tile text",
};

const {
  addTile: addTileToGrid,
  applyLayout,
  currentLayout,
  destroyGrid,
  getTileById,
  gridEl,
  initGrid,
  jsonPreview,
  rebuildGrid,
  setEditHandler,
  switchDevice,
  syncFromValue,
  updateTile,
} = useGridStackLayout(
  props.value,
  (value) => emit("change", value),
  () => activeDevice.value,
);

const TILE_LIMIT = 16;
const tileCount = computed(() => currentLayout.value.tiles.length);
const canAddTile = computed(() => tileCount.value < TILE_LIMIT);

function addTile() {
  if (!canAddTile.value) return;
  addTileToGrid();
}

const {
  cancelEdit,
  cloudinaryImgLoading,
  cloudinaryImBgLoading,
  editContentTitle,
  editBgColor,
  editAfter,
  editBefore,
  editImg,
  editImBg,
  editTileOption,
  editModalOpen,
  openCloudinaryImgPicker,
  openCloudinaryImBgPicker,
  openEditModal,
  saveEditContent,
} = useTileEditor(getTileById, updateTile, rebuildGrid, () => ({
  cloudName: props.cloudName,
  apiKey: props.apiKey,
}));

setEditHandler(openEditModal);

watch(
  () => props.value,
  (newValue) => {
    syncFromValue(newValue);
  },
);

watch(activeDevice, (_newDevice, oldDevice) => {
  switchDevice(oldDevice);
});

function stopVaadinCapture(event) {
  event.stopPropagation();
}

onMounted(() => {
  injectBentoStyles();
  nextTick(() => {
    initGrid();
    initActiveView();
  });
});

onBeforeUnmount(() => {
  destroyGrid();
});

// The "11 items" and "16 items" presets behave as two independent sessions:
// each remembers its own tiles in-memory, so toggling between them never
// discards the work done in the other view (no confirm prompt needed).
const VIEW_16 = "16";
const VIEW_11 = "11";
const activeView = ref(null);
const viewSessions = { [VIEW_11]: null, [VIEW_16]: null };

function cloneLayout(layout) {
  return JSON.parse(JSON.stringify(layout));
}

function initActiveView() {
  // The 16-item preset is the only one exceeding 12 tiles, so use the tile
  // count to decide which session the saved content belongs to.
  const view = currentLayout.value.tiles.length > 12 ? VIEW_16 : VIEW_11;
  activeView.value = view;
  viewSessions[view] = cloneLayout(currentLayout.value);
}

function switchView(view, createDefault) {
  if (activeView.value === view) return;
  if (activeView.value) {
    viewSessions[activeView.value] = cloneLayout(currentLayout.value);
  }
  activeView.value = view;
  const saved = viewSessions[view];
  applyLayout(saved ? cloneLayout(saved) : createDefault());
}

function setView16() {
  switchView(VIEW_16, createDefaultLayout16);
}

function setView11() {
  switchView(VIEW_11, createDefaultLayout);
}
</script>

<style>
*,
*::before,
*::after {
  box-sizing: border-box;
}
.bl {
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  font-size: 13px;
  color: #333;
}
.bl__toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.bl__device-select {
  border: 1px solid #cfd6de;
  border-radius: 4px;
  padding: 6px 8px;
  background: #fff;
  font-size: 12px;
}
.bl__btn {
  padding: 6px 14px;
  border-radius: 4px;
  border: 1px solid #ccc;
  background: #fff;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.15s;
}
.bl__btn:hover {
  background: #f0f0f0;
}
.bl__btn--add {
  border-color: #4a90d9;
  color: #4a90d9;
}
.bl__btn--add-tile {
  border-color: #019c68;
  color: #019c68;
}
.bl__btn--add:hover {
  background: #eaf3ff;
}
.bl__btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.bl__btn:disabled:hover {
  background: #fff;
}
.bl__btn--view-active {
  border-color: #4a90d9;
  background: #4a90d9;
  color: #fff;
}
.bl__btn--view-active:hover {
  background: #3a7cc6;
}
.bl__grid {
  background: #f8f9fa;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  min-height: 300px;
}
.grid-stack-item-content {
  background: #fff !important;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: stretch;
}
.grid-stack > .grid-stack-item > .grid-stack-item-content {
  overflow-x: hidden !important;
  overflow-y: hidden !important;
}
.grid-stack-item-content:hover {
  border-color: #4a90d9;
  box-shadow: 0 2px 8px rgba(74, 144, 217, 0.15);
}
.bl__tile-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  position: relative;
  padding: 6px 8px;
  overflow: hidden;
  user-select: none;
}
.bl__tile-inner.bl__tile-bg--black {
  background: #212529;
}
.bl__tile-inner.bl__tile-bg--white {
  background: #ffffff;
}
.bl__tile-inner.bl__tile-bg--gray-lighter {
  background: #f1f3f5;
}
.bl__tile-text {
  font-size: 12px;
  font-weight: 600;
  color: #495057;
  text-align: center;
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1.2;
}
.bl__tile-fit {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  width: 100%;
  max-width: 100%;
  height: 100%;
  transform-origin: center center;
  word-break: break-word;
  overflow-wrap: anywhere;
}
.bl__tile-before,
.bl__tile-after,
.bl__tile-title {
  flex: 0 0 auto;
  width: 100%;
  min-width: 0;
}
.bl__tile-fit > * {
  max-width: 100%;
}
.bl__tile-text p,
.bl__tile-text h1,
.bl__tile-text h2,
.bl__tile-text h3,
.bl__tile-text h4,
.bl__tile-text h5,
.bl__tile-text h6,
.bl__tile-text ol,
.bl__tile-text ul {
  margin: 0;
  padding: 0;
  line-height: 1.2;
}
.bl__tile-text h1 { font-size: 1.5em; }
.bl__tile-text h2 { font-size: 1.35em; }
.bl__tile-text h3 { font-size: 1.2em; }
.bl__tile-text h4 { font-size: 1.1em; }
.bl__tile-text h5,
.bl__tile-text h6 { font-size: 1em; }
.bl__tile-text ol,
.bl__tile-text ul {
  padding-left: 1.25em;
  list-style-position: inside;
}
.bl__tile-text li {
  margin: 0;
}
.bl__tile-inner.bl__tile-bg--black .bl__tile-text {
  color: #ffffff;
}
.bl__tile-text a {
  color: inherit;
}
.bl__tile-title {
  font-weight: 700;
}
.bl__tile-content-img {
  flex: 0 1 auto;
  min-height: 0;
  width: auto;
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  margin: 0 auto;
  background: #f1f3f5
    url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23adb5bd' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'><rect x='3' y='3' width='18' height='18' rx='2'/><circle cx='8.5' cy='8.5' r='1.5'/><polyline points='21 15 16 10 5 21'/></svg>")
    center / 28px no-repeat;
}
.bl__tile-edit {
  position: absolute;
  top: 4px;
  left: 6px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: #adb5bd;
  border-radius: 3px;
  padding: 2px 4px;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.bl__tile-edit:hover {
  color: #4a90d9;
  background: #eaf3ff;
}
.bl__tile-remove {
  position: absolute;
  top: 4px;
  right: 6px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  color: #adb5bd;
  border-radius: 3px;
  padding: 0 4px;
  transition: all 0.15s;
}
.bl__tile-remove:hover {
  color: #c0392b;
  background: #fdecea;
}
.bl__overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}
.bl__editor {
  display: flex;
  justify-content: center;
  width: 100%;
  padding: 4px 0 8px;
}
.bl__modal {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 760px;
  overflow: hidden;
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}
.bl__modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid #e8e8e8;
}
.bl__modal-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.bl__modal-close {
  border: none;
  background: transparent;
  font-size: 22px;
  cursor: pointer;
  color: #999;
  padding: 0 4px;
  line-height: 1;
}
.bl__modal-close:hover {
  color: #333;
}
.bl__modal-body {
  padding: 18px;
}
.bl__modal-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: #555;
  margin-bottom: 6px;
}
.bl__modal-input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #d0d0d0;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.15s;
}
.bl__modal-input:focus {
  border-color: #4a90d9;
  box-shadow: 0 0 0 3px rgba(74, 144, 217, 0.15);
}
.bl__quill {
  border: 1px solid #d0d0d0;
  border-radius: 4px;
  position: relative;
}
.bl__quill .ql-toolbar.ql-snow {
  border: none;
  border-bottom: 1px solid #e4e7eb;
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
}
.bl__quill .ql-container.ql-snow {
  border: none;
  min-height: 110px;
  border-bottom-left-radius: 4px;
  border-bottom-right-radius: 4px;
}
.bl__quill .ql-tooltip {
  z-index: 20;
  left: 8px !important;
}
.bl__quill .ql-tooltip input[type="text"] {
  width: 220px;
}
.bl__modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 18px;
  border-top: 1px solid #e8e8e8;
  background: #fafafa;
}
.bl__btn--save {
  border-color: #4a90d9;
  background: #4a90d9;
  color: #fff;
}
.bl__btn--save:hover {
  background: #3a7cc6;
}
.bl__preview {
  margin-top: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}
.bl__preview-label {
  padding: 6px 12px;
  background: #f5f5f5;
  cursor: pointer;
  font-size: 11px;
  color: #666;
  user-select: none;
}
.bl__json {
  margin: 0;
  padding: 12px;
  font-size: 11px;
  font-family: monospace;
  background: #1e1e1e;
  color: #9cdcfe;
  overflow-x: auto;
  white-space: pre;
}
.grid-stack-placeholder > .placeholder-content {
  border: 2px dashed #4a90d9 !important;
  background: rgba(74, 144, 217, 0.08) !important;
  border-radius: 6px;
}
.bl__tile-inner--has-image {
  padding: 0;
}
.bl__tile-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 5px;
}
.bl__tile-inner--has-image .bl__tile-text {
  position: relative;
  z-index: 1;
  color: #fff;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.6);
}
.bl__tile-inner--has-image .bl__tile-edit,
.bl__tile-inner--has-image .bl__tile-remove {
  z-index: 2;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}
.bl__tile-inner--has-image .bl__tile-edit:hover {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
}
.bl__tile-inner--has-image .bl__tile-remove:hover {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
}
.bl__modal-section {
  margin-top: 14px;
}
.bl__modal-image-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
}
.bl__responsive-row {
  display: grid;
  grid-template-columns: 62px 62px 62px 62px 62px;
  gap: 8px;
  align-items: end;
  margin-top: 8px;
}
.bl__responsive-name {
  font-size: 12px;
  font-weight: 600;
  color: #556;
}
.bl__responsive-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 11px;
  color: #666;
}
.bl__modal-input--small {
  padding: 6px 8px;
  font-size: 12px;
}
.bl__device-select--toolbar {
  margin-left: 45px;
  margin-right: 45px;
}
.bl__btn--cloudinary {
  border-color: #3448c5;
  color: #3448c5;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.bl__btn--cloudinary:hover {
  background: #eef0ff;
}
.bl__btn--cloudinary:disabled {
  opacity: 0.5;
  cursor: wait;
}
.bl__btn--remove-img {
  border-color: #c0392b;
  color: #c0392b;
  padding: 6px 10px;
  font-size: 11px;
}
.bl__btn--remove-img:hover {
  background: #fdecea;
}
.bl__modal-img-preview {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #dee2e6;
  flex-shrink: 0;
}
.bl__modal-img-url {
  font-size: 11px;
  color: #888;
  word-break: break-all;
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
