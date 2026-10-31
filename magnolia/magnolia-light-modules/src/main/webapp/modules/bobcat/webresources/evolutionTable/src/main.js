import { createApp, h, reactive } from "vue";
import EvolutionTable from "./EvolutionTable.vue";

class MgnlEvolutionTable extends HTMLElement {
  static get observedAttributes() {
    return ["value"];
  }

  connectedCallback() {
    const mountPoint = document.createElement("div");
    this.appendChild(mountPoint);

    const state = reactive({
      value: this.getAttribute("value") || "",
    });
    this._state = state;
    const el = this;

    this._app = createApp({
      setup() {
        return () =>
          h(EvolutionTable, {
            value: state.value,
            onChange: (json) => {
              el.dispatchEvent(
                new CustomEvent("change", { detail: [json], bubbles: true }),
              );
            },
          });
      },
    });
    this._app.mount(mountPoint);
  }

  attributeChangedCallback(name, oldVal, newVal) {
    if (name === "value" && this._state) {
      this._state.value = newVal || "";
    }
  }

  disconnectedCallback() {
    if (this._app) {
      this._app.unmount();
      this._app = null;
    }
  }
}

if (!customElements.get("mgnl-evolution-table")) {
  customElements.define("mgnl-evolution-table", MgnlEvolutionTable);
}
