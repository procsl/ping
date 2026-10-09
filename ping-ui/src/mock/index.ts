/**
 * 前端模拟数据：渲染器暂不依赖后端接口，全部数据源收敛于此，
 * 后续接入 GET /v1/ui/menus 时只需替换数据装配层，组件结构不变。
 */

export interface MockUser {
  account: string
  nickname: string
  role: string
  avatarText: string
}

export const mockUser: MockUser = {
  account: "admin",
  nickname: "管理员",
  role: "超级管理员",
  avatarText: "A",
}

export interface MockStat {
  title: string
  value: string
}

export const mockStats: MockStat[] = [
  { title: "今日访问", value: "1,280" },
  { title: "接口调用", value: "8,642" },
  { title: "在线用户", value: "36" },
  { title: "异常告警", value: "0" },
]

export interface MockRow {
  id: string
  account: string
  nickname: string
  status: string
}

export const mockUserRows: MockRow[] = [
  { id: "1", account: "admin", nickname: "管理员", status: "启用" },
  { id: "2", account: "procsl", nickname: "开发者", status: "启用" },
  { id: "3", account: "guest", nickname: "访客", status: "停用" },
]
