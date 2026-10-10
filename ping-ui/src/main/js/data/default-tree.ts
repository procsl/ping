import type { ComponentNode } from "@/schema/types"

/**
 * 本地组件树（当前唯一数据源，与后端接口解耦）。
 * 路由统一两段式：/xxx/xxx；左侧菜单为 6 个分组、15 个菜单项，
 * 用于验证侧栏在多菜单规模下的布局、滚动与搜索表现。
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
            {
              type: "menu",
              id: "menu.system.role",
              name: "角色管理",
              router: "system/role",
              order: 2,
              containers: [
                { type: "placeholder", id: "ph.system.role", name: "角色管理" },
              ],
            },
            {
              type: "menu",
              id: "menu.system.menu",
              name: "菜单管理",
              router: "system/menu",
              order: 3,
              containers: [
                { type: "placeholder", id: "ph.system.menu", name: "菜单管理" },
              ],
            },
            {
              type: "menu",
              id: "menu.system.dict",
              name: "数据字典",
              router: "system/dict",
              order: 4,
              containers: [
                { type: "placeholder", id: "ph.system.dict", name: "数据字典" },
              ],
            },
          ],
        },
        {
          type: "menu",
          id: "menu.authz",
          name: "权限中心",
          order: 3,
          containers: [
            {
              type: "menu",
              id: "menu.authz.grant",
              name: "授权管理",
              router: "authz/grant",
              order: 1,
              containers: [
                { type: "placeholder", id: "ph.authz.grant", name: "授权管理" },
              ],
            },
            {
              type: "menu",
              id: "menu.authz.policy",
              name: "访问策略",
              router: "authz/policy",
              order: 2,
              containers: [
                { type: "placeholder", id: "ph.authz.policy", name: "访问策略" },
              ],
            },
            {
              type: "menu",
              id: "menu.authz.org",
              name: "组织架构",
              router: "authz/org",
              order: 3,
              containers: [
                { type: "placeholder", id: "ph.authz.org", name: "组织架构" },
              ],
            },
          ],
        },
        {
          type: "menu",
          id: "menu.config",
          name: "配置中心",
          order: 4,
          containers: [
            {
              type: "menu",
              id: "menu.config.param",
              name: "参数配置",
              router: "config/param",
              order: 1,
              containers: [
                { type: "placeholder", id: "ph.config.param", name: "参数配置" },
              ],
            },
            {
              type: "menu",
              id: "menu.config.feature",
              name: "功能开关",
              router: "config/feature",
              order: 2,
              containers: [
                {
                  type: "placeholder",
                  id: "ph.config.feature",
                  name: "功能开关",
                },
              ],
            },
          ],
        },
        {
          type: "menu",
          id: "menu.audit",
          name: "日志审计",
          order: 5,
          containers: [
            {
              type: "menu",
              id: "menu.audit.operation",
              name: "操作日志",
              router: "audit/operation",
              order: 1,
              containers: [
                {
                  type: "placeholder",
                  id: "ph.audit.operation",
                  name: "操作日志",
                },
              ],
            },
            {
              type: "menu",
              id: "menu.audit.login",
              name: "登录日志",
              router: "audit/login",
              order: 2,
              containers: [
                {
                  type: "placeholder",
                  id: "ph.audit.login",
                  name: "登录日志",
                },
              ],
            },
          ],
        },
        {
          type: "menu",
          id: "menu.monitor",
          name: "监控运维",
          order: 6,
          containers: [
            {
              type: "menu",
              id: "menu.monitor.status",
              name: "服务状态",
              router: "monitor/status",
              order: 1,
              containers: [
                {
                  type: "placeholder",
                  id: "ph.monitor.status",
                  name: "服务状态",
                },
              ],
            },
            {
              type: "menu",
              id: "menu.monitor.online",
              name: "在线用户",
              router: "monitor/online",
              order: 2,
              containers: [
                {
                  type: "placeholder",
                  id: "ph.monitor.online",
                  name: "在线用户",
                },
              ],
            },
            {
              type: "menu",
              id: "menu.monitor.job",
              name: "任务调度",
              router: "monitor/job",
              order: 3,
              containers: [
                {
                  type: "placeholder",
                  id: "ph.monitor.job",
                  name: "任务调度",
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
