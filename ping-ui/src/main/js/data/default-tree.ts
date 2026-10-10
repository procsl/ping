import type { ComponentNode } from "@/schema/types"

/**
 * 本地组件树：后端接口不可用时的降级声明，与接口返回的结构同构。
 * 正常数据源是 GET /v1/ui/components（见 data/loader.ts）。
 * 路由统一两段式：/xxx/xxx；左侧菜单为 6 个分组、15 个菜单项，
 * 用于验证侧栏在多菜单规模下的布局、滚动与搜索表现。
 * 服务端同构声明见 ping-ui/src/main/resources/ui/app/compose.json。
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
                  type: "dataset",
                  id: "table.system.user",
                  name: "用户管理",
                  api: { method: "GET", path: "/v1/system/users" },
                  row_key: "id",
                  selectable: true,
                  containers: [
                    {
                      type: "query",
                      id: "query.system.user",
                      name: "查询条件",
                      containers: [
                        {
                          type: "field",
                          id: "f.account",
                          name: "account",
                          label: "账号",
                          widget: "input",
                          placeholder: "请输入账号",
                        },
                        {
                          type: "field",
                          id: "f.status",
                          name: "status",
                          label: "状态",
                          widget: "select",
                          options: [
                            { value: "1", label: "启用" },
                            { value: "0", label: "停用" },
                          ],
                        },
                      ],
                    },
                    {
                      type: "column",
                      id: "col.account",
                      field: "account",
                      name: "账号",
                      sortable: true,
                    },
                    { type: "column", id: "col.nickname", field: "nickname", name: "昵称" },
                    {
                      type: "column",
                      id: "col.status",
                      field: "status",
                      name: "状态",
                      sortable: true,
                      format: "enum",
                      enum: { "1": "启用", "0": "停用" },
                    },
                    { type: "action", id: "action.edit", name: "编辑", placement: "toolbar" },
                    {
                      type: "row_action",
                      id: "ra.reset",
                      name: "重置密码",
                      confirm: "确认重置该用户的登录密码？",
                      danger: true,
                    },
                    {
                      type: "row_action",
                      id: "ra.toggle_status",
                      name: "切换状态",
                      confirm: "确认切换该用户的状态？",
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
