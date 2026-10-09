import type { ComponentNode } from "@/schema/types"

/**
 * 本地声明的默认组件树（开发/降级数据源）。
 * 待后端 GET /v1/ui/menus 就绪后，同一结构由接口返回，前端无需发版。
 * 路由统一为两段式：/xxx/xxx
 */
export const defaultTree: ComponentNode = {
  type: "application",
  id: "ping.ui.app",
  name: "Ping Admin",
  containers: [
    {
      type: "layout",
      layout: "left",
      containers: [
        {
          type: "menu",
          id: "menu.home",
          name: "首页",
          order: 1,
          containers: [
            {
              type: "menu",
              id: "menu.home.dashboard",
              name: "工作台",
              router: "home/dashboard",
              order: 1,
            },
          ],
        },
        {
          type: "menu",
          id: "menu.system",
          name: "系统管理",
          order: 2,
          containers: [
            {
              type: "menu",
              id: "menu.system.user",
              name: "用户管理",
              router: "system/user",
              order: 1,
              containers: [
                {
                  type: "table",
                  id: "table.system.user",
                  apis: [{ method: "GET", api: "/v1/system/users" }],
                  containers: [
                    { type: "column", id: "col.username", name: "账号" },
                    { type: "column", id: "col.nickname", name: "昵称" },
                    { type: "column", id: "col.status", name: "状态" },
                    {
                      type: "action",
                      id: "action.edit",
                      name: "编辑",
                    },
                    {
                      type: "action",
                      id: "action.reset",
                      name: "重置密码",
                    },
                  ],
                },
              ],
            },
            {
              type: "menu",
              id: "menu.system.role",
              name: "角色管理",
              router: "system/role",
              order: 2,
            },
            {
              type: "menu",
              id: "menu.system.menu",
              name: "菜单管理",
              router: "system/menu",
              order: 3,
            },
          ],
        },
        {
          type: "menu",
          id: "menu.monitor",
          name: "监控中心",
          order: 3,
          containers: [
            {
              type: "menu",
              id: "menu.monitor.online",
              name: "在线用户",
              router: "monitor/online",
              order: 1,
            },
          ],
        },
      ],
    },
    {
      type: "layout",
      layout: "right",
      containers: [
        {
          type: "layout",
          layout: "top",
          containers: [
            {
              type: "user_info_panel",
              id: "user_info_panel",
              apis: [{ method: "GET", api: "/v1/system/authentications" }],
              description: "用户信息面板",
            },
          ],
        },
        {
          type: "layout",
          layout: "bottom",
          containers: [{ type: "main_container", id: "main_container" }],
        },
      ],
    },
  ],
}
