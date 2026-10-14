import path from "node:path"
import { defineConfig } from "vite"

/**
 * demo 专用 vite 配置：以 `src/main/js/sdk/demo/` 为 root 起 dev server。
 * 与主构建（vite.config.ts 的 lib 模式）互不影响，demo 不进 build 产物。
 */
export default defineConfig({
  root: path.resolve(__dirname, "src/main/js/sdk/demo"),
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src/main/js"),
    },
  },
  server: {
    open: true,
  },
})
