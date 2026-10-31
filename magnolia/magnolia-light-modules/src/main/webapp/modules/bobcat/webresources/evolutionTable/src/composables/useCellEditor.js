import { ref } from "vue";
import {
  COLOR_OPTIONS,
  LINK_TARGETS,
  LINK_TYPES,
} from "../utils/evolutionTable.js";

function emptyEditState() {
  return {
    type: "empty",
    value: "",
    color: "default",
    bold: false,
    wrap: true,
    link: {
      type: "none",
      pageLink: "",
      externalUrl: "",
      target: "_self",
    },
  };
}

export function useCellEditor(saveCallback) {
  const modalOpen = ref(false);
  const context = ref(null);
  const draft = ref(emptyEditState());

  function open(ctx, source) {
    context.value = ctx;
    draft.value = {
      type: source?.type || "empty",
      value: source?.value || "",
      color: COLOR_OPTIONS.includes(source?.color) ? source.color : "default",
      bold: Boolean(source?.bold),
      wrap: source?.wrap !== false,
      link: {
        type: LINK_TYPES.includes(source?.link?.type)
          ? source.link.type
          : "none",
        pageLink: source?.link?.pageLink || "",
        externalUrl: source?.link?.externalUrl || "",
        target: LINK_TARGETS.includes(source?.link?.target)
          ? source.link.target
          : "_self",
      },
    };
    modalOpen.value = true;
  }

  function close() {
    modalOpen.value = false;
    context.value = null;
    draft.value = emptyEditState();
  }

  function save() {
    if (!context.value) {
      close();
      return;
    }
    saveCallback(context.value, { ...draft.value, link: { ...draft.value.link } });
    close();
  }

  return {
    modalOpen,
    context,
    draft,
    open,
    close,
    save,
  };
}
