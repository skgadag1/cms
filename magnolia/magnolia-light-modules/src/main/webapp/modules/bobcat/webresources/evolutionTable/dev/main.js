import { createApp, h, reactive } from "vue";
import EvolutionTable from "../src/EvolutionTable.vue";

const state = reactive({ value: "" });

createApp({
  setup() {
    return () =>
      h(EvolutionTable, {
        value: state.value,
        onChange: (json) => {
          state.value = json;
        },
      });
  },
}).mount("#app");
