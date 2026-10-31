import { nextTick, ref } from "vue";
import { DEVICES, getTileDeviceSettings } from "../utils/bentoWidget";
import { useCloudinaryPicker } from "./useCloudinaryPicker";

export function useTileEditor(
  getTileById,
  updateTile,
  rebuildGrid,
  getCloudConfig,
) {
  const editModalOpen = ref(false);
  const editTileId = ref(null);
  const editTileOption = ref("title");
  const editContentTitle = ref("");
  const editImg = ref("");
  const editImBg = ref("");
  const editBgColor = ref("none");
  const editBefore = ref("");
  const editAfter = ref("");
  const editResponsiveSettings = ref({
    mobile: { x: 0, y: 0, w: 1, h: 1 },
    tablet: { x: 0, y: 0, w: 1, h: 1 },
    desktop: { x: 0, y: 0, w: 1, h: 1 },
  });

  const {
    cloudinaryLoading: cloudinaryImgLoading,
    openCloudinaryPicker: openCloudinaryImgPicker,
  } = useCloudinaryPicker(editImg, getCloudConfig);
  const {
    cloudinaryLoading: cloudinaryImBgLoading,
    openCloudinaryPicker: openCloudinaryImBgPicker,
  } = useCloudinaryPicker(editImBg, getCloudConfig);

  function openEditModal(tileId) {
    const tile = getTileById(tileId);
    if (!tile) return;

    editTileId.value = tileId;
    editTileOption.value = tile.tileOption || "title";
    editContentTitle.value = tile.contentTitle || "";
    editImg.value = tile.img || "";
    editImBg.value = tile.imBg || "";
    editBgColor.value = tile.bgColor || "none";
    editBefore.value = tile.before || tile.text || tile.content || "";
    editAfter.value = tile.after || "";
    editResponsiveSettings.value = DEVICES.reduce((acc, device) => {
      const settings = getTileDeviceSettings(tile, device);
      acc[device] = {
        x: settings.x,
        y: settings.y,
        w: settings.w,
        h: settings.h,
      };
      return acc;
    }, {});
    editModalOpen.value = true;

    nextTick(() => {
      document.querySelector(".ql-editor")?.focus();
    });
  }

  function saveEditContent() {
    if (!editTileId.value) return;

    const ok = updateTile(editTileId.value, {
      tileOption: editTileOption.value || "title",
      contentTitle: editContentTitle.value,
      img: editImg.value || undefined,
      imBg: editImBg.value || undefined,
      bgColor: editBgColor.value || "none",
      before: editBefore.value,
      after: editAfter.value,
      responsive: DEVICES.reduce((acc, device) => {
        const deviceSettings = editResponsiveSettings.value[device] || {};
        acc[device] = {
          x: Math.max(0, Number(deviceSettings.x) || 0),
          y: Math.max(0, Number(deviceSettings.y) || 0),
          w: Math.max(1, Number(deviceSettings.w) || 1),
          h: Math.max(1, Number(deviceSettings.h) || 1),
        };
        return acc;
      }, {}),
    });

    if (!ok) {
      cancelEdit();
      return;
    }

    editModalOpen.value = false;
    editTileId.value = null;

    // Grid is hidden (v-show) while editing; restore it first, then rebuild so
    // GridStack measures against a visible element.
    nextTick(() => rebuildGrid());
  }

  function cancelEdit() {
    editModalOpen.value = false;
    editTileId.value = null;
    editTileOption.value = "title";
    editContentTitle.value = "";
    editImg.value = "";
    editImBg.value = "";
    editBgColor.value = "none";
    editBefore.value = "";
    editAfter.value = "";
    editResponsiveSettings.value = {
      mobile: { x: 0, y: 0, w: 1, h: 1 },
      tablet: { x: 0, y: 0, w: 1, h: 1 },
      desktop: { x: 0, y: 0, w: 1, h: 1 },
    };
  }

  return {
    cancelEdit,
    cloudinaryImgLoading,
    cloudinaryImBgLoading,
    editTileOption,
    editContentTitle,
    editImg,
    editImBg,
    editBgColor,
    editAfter,
    editBefore,
    editModalOpen,
    editResponsiveSettings,
    openCloudinaryImgPicker,
    openCloudinaryImBgPicker,
    openEditModal,
    saveEditContent,
  };
}
