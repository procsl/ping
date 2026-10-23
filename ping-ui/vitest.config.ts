import { defineConfig } from "vitest/config"

// 独立测试配置：client 为纯逻辑模块，node 环境即真实运行环境（D9）
export default defineConfig({
  test: {
    environment: "node",
    include: ["src/**/*.test.ts"],
    // 基座阶段允许 0 用例（测试随任务 2.x-4.x 逐个落地）
    passWithNoTests: true,
  },
})
