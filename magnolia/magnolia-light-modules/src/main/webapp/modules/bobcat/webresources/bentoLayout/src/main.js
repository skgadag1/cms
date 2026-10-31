import { createApp, h, reactive } from "vue";
import BentoLayout from "./BentoLayout.vue";

class MgnlBentoLayout extends HTMLElement {
  static get observedAttributes() {
    return ["value", "cloud-name", "api-key"];
  }

  connectedCallback() {
    const mountPoint = document.createElement("div");
    this.appendChild(mountPoint);

    const state = reactive({
      value: this.getAttribute("value") || "",
      cloudName: this.getAttribute("cloud-name") || "",
      apiKey: this.getAttribute("api-key") || "",
    });
    this._state = state;
    const el = this;

    this._app = createApp({
      setup() {
        return () =>
          h(BentoLayout, {
            value: state.value,
            cloudName: state.cloudName,
            apiKey: state.apiKey,
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
    if (!this._state) return;
    if (name === "value") {
      this._state.value = newVal || "";
    } else if (name === "cloud-name") {
      this._state.cloudName = newVal || "";
    } else if (name === "api-key") {
      this._state.apiKey = newVal || "";
    }
  }

  disconnectedCallback() {
    if (this._app) {
      this._app.unmount();
      this._app = null;
    }
  }
}

if (!customElements.get("mgnl-bento-layout")) {
  customElements.define("mgnl-bento-layout", MgnlBentoLayout);
}
