import path from "node:path"
import react from "@vitejs/plugin-react"
import tailwindcss from "@tailwindcss/vite"
import { defineConfig } from "vite"

// 后端本地开发端口（与 ping-web 的 web.server.port 保持一致）
const backendPort = process.env.PING_BACKEND_PORT ?? "10000"

export default defineConfig(({ mode }) => ({
  plugins: [react(), tailwindcss()],
  // 相对路径：薄壳位于 /<module>/index.html、主壳位于 /index.html，两级目录下均可解析
  base: "./",
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 5173,
    proxy: {
      "/v1": {
        target: `http://localhost:${backendPort}`,
        changeOrigin: true,
      },
    },
  },
  build: {
    // 渲染器以 ESM 库形式产出，供主壳 / 薄壳 import
    lib: {
      entry: path.resolve(__dirname, "src/main.tsx"),
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
