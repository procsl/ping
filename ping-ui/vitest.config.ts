import { defineConfig } from "vitest/config"

// 独立测试配置：client 为纯逻辑模块，node 环境即真实运行环境（D9）
export default defineConfig({
  test: {
    environment: "node",
    include: ["src/main/js/**/*.test.ts"],
  },
})
