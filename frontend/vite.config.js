import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [react()],
  server: {
    host: "0.0.0.0",
    port: process.env.PORT ? Number(process.env.PORT) : 5173
  },
  preview: {
    host: "0.0.0.0",
    port: process.env.PORT ? Number(process.env.PORT) : 4173
  },
  test: {
    environment: "jsdom",
    setupFiles: "./src/test/setup.js"
  }
});
