import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  plugins: [vue()],
  define: {
    "process.env.NODE_ENV": JSON.stringify("production"),
  },
  build: {
    lib: {
      entry: "src/main.js",
      formats: ["iife"],
      name: "MagnoliaEvolutionTable",
      fileName: () => "evolution-table.js",
    },
    emptyOutDir: true,
    cssCodeSplit: false,
    rollupOptions: {
      output: {
        inlineDynamicImports: true,
        assetFileNames: "evolution-table[extname]",
      },
    },
  },
});
