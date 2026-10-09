import { mount } from "@/main"

// 开发入口：vite dev server 下运行，接口经 proxy 转发至后端 10000 端口
mount({
  namespace: "ui",
  manifest: "/v1/ui/menus?ns=ui",
})
