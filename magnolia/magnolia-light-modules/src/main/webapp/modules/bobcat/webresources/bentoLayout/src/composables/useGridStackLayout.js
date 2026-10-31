import { ref } from "vue";
import { GridStack } from "gridstack";
import { createDefaultLayout } from "../constants/defaultLayout";
import {
  DEFAULT_DEVICE,
  getTileDeviceSettings,
  makeWidgetHtml,
  nextAvailableId,
  normalizeLayout,
  parseLayout,
} from "../utils/bentoWidget";

export function useGridStackLayout(
  initialValue,
  emitLayoutChange,
  getActiveDevice,
) {
  let grid = null;
  let suppressSync = false;
  let localChangeInFlight = false;
  let editHandler = () => {};
  let fitObserver = null;
  let fitFrame = null;
  let onFontsLoaded = null;
  const FIT_SAFETY = 2;

  const gridEl = ref(null);
  const currentLayout = ref(
    normalizeLayout(parseLayout(initialValue, createDefaultLayout)),
  );
  const jsonPreview = ref(JSON.stringify(currentLayout.value, null, 2));

  function activeDevice() {
    return getActiveDevice?.() || DEFAULT_DEVICE;
  }

  function emitChange() {
    localChangeInFlight = true;
    emitLayoutChange(JSON.stringify(currentLayout.value));
  }

  function syncFromValue(value) {
    if (localChangeInFlight) {
      localChangeInFlight = false;
      return;
    }

    currentLayout.value = normalizeLayout(
      parseLayout(value, createDefaultLayout),
    );
    jsonPreview.value = JSON.stringify(currentLayout.value, null, 2);
    rebuildGrid();
  }

  function getTileById(tileId) {
    return currentLayout.value.tiles.find((tile) => tile.id === tileId) || null;
  }

  function updateTile(tileId, update) {
    const tile = getTileById(tileId);
    if (!tile) return false;

    Object.assign(tile, update);
    currentLayout.value = normalizeLayout(currentLayout.value);
    jsonPreview.value = JSON.stringify(currentLayout.value, null, 2);
    emitChange();
    return true;
  }

  function applyLayout(layout) {
    currentLayout.value = normalizeLayout(layout);
    jsonPreview.value = JSON.stringify(currentLayout.value, null, 2);
    rebuildGrid();
    emitChange();
  }

  function getGridItems() {
    if (!grid) return [];
    return grid.getGridItems();
  }

  function extractLayout(deviceOverride) {
    if (!grid) return;

    const device = deviceOverride || activeDevice();
    const existing = new Map(
      currentLayout.value.tiles.map((tile) => [tile.id, tile]),
    );
    const seen = new Set();
    const tiles = [];

    for (const el of grid.getGridItems()) {
      const node = el.gridstackNode;
      if (!node) continue;

      const id = node.id || el.getAttribute("gs-id");
      if (!id || seen.has(id)) continue;

      seen.add(id);

      const baseTile = existing.get(id)
        ? { ...existing.get(id) }
        : {
            id,
            x: node.x,
            y: node.y,
            w: node.w,
            h: node.h,
            tileOption: "title",
            contentTitle: "",
            img: "",
            imBg: "",
            before: "",
            after: "",
            bgColor: "none",
          };

      baseTile.x = node.x;
      baseTile.y = node.y;
      baseTile.w = node.w;
      baseTile.h = node.h;
      baseTile.tileOption = node._tileOption || baseTile.tileOption || "title";
      baseTile.contentTitle = node._contentTitle || baseTile.contentTitle || "";
      baseTile.img = node._img || baseTile.img || "";
      baseTile.imBg = node._imBg || baseTile.imBg || "";
      baseTile.before =
        node._before ||
        el.querySelector(".bl__tile-before")?.innerHTML ||
        baseTile.before ||
        baseTile.text ||
        "";
      baseTile.after =
        node._after ||
        el.querySelector(".bl__tile-after")?.innerHTML ||
        baseTile.after ||
        "";
      baseTile.bgColor = node._bgColor || baseTile.bgColor || "none";
      delete baseTile.content;
      delete baseTile.text;

      const settings = getTileDeviceSettings(baseTile, device);
      baseTile.responsive = baseTile.responsive || {};
      baseTile.responsive[device] = {
        x: node.x,
        y: node.y,
        w: node.w,
        h: node.h,
      };

      tiles.push(baseTile);
    }

    currentLayout.value = normalizeLayout({ tiles });
    jsonPreview.value = JSON.stringify(currentLayout.value, null, 2);
    emitChange();
  }

  function fitTileText(outer) {
    const fit = outer.querySelector(".bl__tile-fit");
    if (!fit) return;
    fit.style.transform = "";
    const availH = fit.clientHeight;
    if (availH <= 0) return;
    const gap = parseFloat(getComputedStyle(fit).rowGap) || 0;
    const children = Array.from(fit.children);
    let contentH = gap * Math.max(0, children.length - 1);
    children.forEach((el) => { contentH += el.offsetHeight; });
    if (contentH > availH + 1) {
      const scale = Math.max(0.1, (availH - FIT_SAFETY) / contentH);
      fit.style.transform = `scale(${scale})`;
    }
  }

  function refreshFit() {
    if (!gridEl.value) return;
    if (!fitObserver && typeof ResizeObserver !== "undefined") {
      fitObserver = new ResizeObserver((entries) => {
        for (const entry of entries) {
          const outer = entry.target.closest(".bl__tile-text");
          if (outer) fitTileText(outer);
        }
      });
    }
    if (fitObserver) fitObserver.disconnect();
    gridEl.value.querySelectorAll(".bl__tile-text").forEach((outer) => {
      fitTileText(outer);
      if (fitObserver) fitObserver.observe(outer);
    });
  }

  function scheduleFit() {
    if (typeof requestAnimationFrame === "undefined") {
      refreshFit();
      return;
    }
    if (fitFrame) cancelAnimationFrame(fitFrame);
    fitFrame = requestAnimationFrame(() => {
      fitFrame = null;
      refreshFit();
    });
  }

  function loadTiles(tiles) {
    if (!grid) return;

    tiles.forEach((tile) => {
      const settings = getTileDeviceSettings(tile, activeDevice());
      const el = grid.addWidget({
        id: tile.id,
        x: settings.x,
        y: settings.y,
        w: settings.w,
        h: settings.h,
        content: makeWidgetHtml(tile, activeDevice()),
      });

      if (el && el.gridstackNode) {
        el.gridstackNode._tileOption = tile.tileOption;
        el.gridstackNode._contentTitle = tile.contentTitle;
        el.gridstackNode._img = tile.img;
        el.gridstackNode._imBg = tile.imBg;
        el.gridstackNode._bgColor = tile.bgColor;
        el.gridstackNode._before = tile.before;
        el.gridstackNode._after = tile.after;
      }
    });

    scheduleFit();
  }

  function clearGrid() {
    if (!grid) return;
    suppressSync = true;
    grid.removeAll(true);
    suppressSync = false;
  }

  function rebuildGrid() {
    if (!grid) return;
    clearGrid();
    loadTiles(currentLayout.value.tiles);
  }

  function switchDevice(previousDevice) {
    if (previousDevice) {
      extractLayout(previousDevice);
    }
    clearGrid();
    loadTiles(currentLayout.value.tiles);
  }

  function removeTile(tileId) {
    if (!grid) return;

    const items = grid.getGridItems();
    const target = items.find(
      (el) => (el.gridstackNode?.id || el.getAttribute("gs-id")) === tileId,
    );

    if (!target) return;

    grid.removeWidget(target);
    scheduleFit();
    extractLayout();
  }

  function addTile() {
    const id = nextAvailableId(currentLayout.value.tiles);
    const device = activeDevice();
    let bottomY = 0;
    for (const existing of currentLayout.value.tiles) {
      const settings = getTileDeviceSettings(existing, device);
      bottomY = Math.max(bottomY, (settings.y ?? 0) + (settings.h ?? 1));
    }
    const tile = normalizeLayout({
      tiles: [
        {
          id,
          x: 0,
          y: bottomY,
          w: 1,
          h: 1,
          tileOption: "title",
          contentTitle: "",
          img: "",
          imBg: "",
          before: `Upper ${id}`,
          after: `Lower ${id}`,
          bgColor: "none",
        },
      ],
    }).tiles[0];

    if (grid) {
      const settings = getTileDeviceSettings(tile, activeDevice());
      const el = grid.addWidget({
        id: tile.id,
        x: settings.x,
        y: settings.y,
        w: settings.w,
        h: settings.h,
        content: makeWidgetHtml(tile, activeDevice()),
      });

      if (el && el.gridstackNode) {
        el.gridstackNode._tileOption = tile.tileOption;
        el.gridstackNode._contentTitle = tile.contentTitle;
        el.gridstackNode._img = tile.img;
        el.gridstackNode._imBg = tile.imBg;
        el.gridstackNode._bgColor = tile.bgColor;
        el.gridstackNode._before = tile.before;
        el.gridstackNode._after = tile.after;
      }
    }

    scheduleFit();
    extractLayout();
  }

  function setEditHandler(handler) {
    editHandler = handler || (() => {});
  }

  function initGrid() {
    if (!gridEl.value) return;

    grid = GridStack.init(
      {
        column: 6,
        cellHeight: 158,
        margin: 6,
        animate: true,
        float: true,
        disableOneColumnMode: true,
        styleInHead: true,
      },
      gridEl.value,
    );

    loadTiles(currentLayout.value.tiles);

    grid.on("dragstop", () => {
      if (!suppressSync) extractLayout();
    });

    grid.on("resizestop", () => {
      if (!suppressSync) extractLayout();
    });

    gridEl.value.addEventListener("click", (event) => {
      const removeBtn = event.target.closest(".bl__tile-remove");
      if (removeBtn) {
        removeTile(removeBtn.getAttribute("data-tile-id"));
        return;
      }

      const editBtn = event.target.closest(".bl__tile-edit");
      if (editBtn) {
        editHandler(editBtn.getAttribute("data-tile-id"));
      }
    });

    if (typeof document !== "undefined" && document.fonts) {
      if (document.fonts.ready) {
        document.fonts.ready.then(() => scheduleFit());
      }
      onFontsLoaded = () => scheduleFit();
      document.fonts.addEventListener("loadingdone", onFontsLoaded);
    }
  }

  function destroyGrid() {
    if (fitFrame && typeof cancelAnimationFrame !== "undefined") {
      cancelAnimationFrame(fitFrame);
      fitFrame = null;
    }
    if (fitObserver) {
      fitObserver.disconnect();
      fitObserver = null;
    }
    if (onFontsLoaded && typeof document !== "undefined" && document.fonts) {
      document.fonts.removeEventListener("loadingdone", onFontsLoaded);
      onFontsLoaded = null;
    }
    if (!grid) return;
    grid.destroy(false);
    grid = null;
  }

  return {
    addTile,
    applyLayout,
    currentLayout,
    destroyGrid,
    extractLayout,
    getTileById,
    getGridItems,
    gridEl,
    initGrid,
    jsonPreview,
    rebuildGrid,
    setEditHandler,
    switchDevice,
    syncFromValue,
    updateTile,
  };
}
