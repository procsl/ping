import path from "node:path"
import { defineConfig } from "vitest/config"

export default defineConfig({
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src/main/js"),
    },
  },
  test: {
    include: ["src/main/js/**/*.test.ts"],
    environment: "node",
  },
})
