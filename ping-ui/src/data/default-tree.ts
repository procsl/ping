import type { ComponentNode } from "@/schema/types"

/**
 * 本地组件树（当前唯一数据源，与后端接口解耦）。
 * 路由统一两段式：/xxx/xxx；菜单只保留两项以验证框架效果。
 * 后续接入 GET /v1/ui/menus 时由接口返回同构数据，前端无需发版。
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
                    { type: "column", id: "col.account", name: "账号" },
                    { type: "column", id: "col.nickname", name: "昵称" },
                    { type: "column", id: "col.status", name: "状态" },
                    { type: "action", id: "action.edit", name: "编辑" },
                    { type: "action", id: "action.reset", name: "重置密码" },
                  ],
                },
              ],
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
              description: "用户信息面板（mock）",
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
