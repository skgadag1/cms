import { createApp, h, reactive } from "vue";
import BentoLayout from "../src/BentoLayout.vue";

const state = reactive({ value: "" });

createApp({
  setup() {
    return () =>
      h(BentoLayout, {
        value: state.value,
        onChange: (json) => {
          state.value = json;
          console.log("bentoLayout change", json);
        },
      });
  },
}).mount("#app");
