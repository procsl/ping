import { mount } from "@/main"

// 开发入口：vite dev server 下运行。数据暂用本地 mock（与后端接口解耦），
// 后续接入时改为 mount({ namespace: 'ui', manifest: '/v1/ui/menus?ns=ui' })
mount({ namespace: "ui" })
