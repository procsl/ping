import path from "node:path"
import react from "@vitejs/plugin-react"
import tailwindcss from "@tailwindcss/vite"
import { defineConfig } from "vite"

export default defineConfig(({ mode }) => ({
  plugins: [react(), tailwindcss()],
  // lib 模式不会自动替换 process.env.NODE_ENV，浏览器端会报 process is not defined
  define: {
    "process.env.NODE_ENV": JSON.stringify(
      mode === "production" ? "production" : "development",
    ),
  },
  // 相对路径：薄壳位于 /<module>/index.html，两级目录下均可解析
  base: "./",
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src/main/js"),
    },
  },
  build: {
    // 渲染器以 ESM 库形式产出，供薄壳 import；产物名保持 renderer.js（薄壳路径写死）
    lib: {
      entry: path.resolve(__dirname, "src/main/js/sdk/index.ts"),
      formats: ["es"],
      fileName: () => "renderer.js",
    },
    outDir: mode === "development" ? "target/dist" : "target/dist",
    emptyOutDir: true,
    sourcemap: mode !== "production",
    rollupOptions: {
      // 固定产物名（不带 hash），薄壳模板才能写死 import 路径
      output: {
        assetFileNames: "renderer[extname]",
        entryFileNames: "renderer.js",
        chunkFileNames: "chunks/[name].js",
      },
    },
  },
}))
