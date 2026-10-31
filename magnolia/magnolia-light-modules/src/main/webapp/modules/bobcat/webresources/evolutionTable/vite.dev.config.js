import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [vue()],
  root: path.join(__dirname, "dev"),
  publicDir: false,
  resolve: {
    alias: {
      "@": path.join(__dirname, "src"),
    },
  },
  server: {
    port: 5174,
    open: true,
  },
});
