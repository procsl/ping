# 阶段 6 设计：Neovim 组件层

对应 `tasks.md` 阶段 6（任务 6.1–6.15）。本文档承载实现级细节。架构决策见 `../design.md` D3（组件库原语）、D4（渲染两条路径）、D5（流式写入纪律）、D6（代码高亮）。

> 注：本文档由 design.md 迁移而来，尚未按细化节奏展开；后续细化时补充组件类型清单与属性。

## 社区库清单

全部 MIT，与本项目 MIT 兼容：

| 库 | 许可 | 提供 | 在本设计中的角色 |
|---|---|---|---|
| `nui.nvim` | MIT | text / line / table / **tree** / layout / popup / input / menu / split | 原语层：布局、弹层、菜单 |
| `nui-components.nvim` | MIT | 响应式状态、表单校验、layout-engine、树（基于 nui） | 状态层：布局区联动 |
| `render-markdown.nvim` | MIT | extmark 就地渲染 markdown，只画可见内容 | 对话 buffer 的 markdown 呈现 |
| *(参考) `sllm.nvim`* | Apache-2.0 | 后端外部 CLI + 插件只做 UI 编排，约 500 行 Lua | 架构先例，不引入 |
| *(禁用) CopilotChat.nvim* | **GPL-3** | 成熟聊天 UI | 只借思路，**不引入代码** |

参考先例：`sllm.nvim` 证明"外部进程做 agent + 插件只做 UI 粘合 + UI 全用社区库"这条路线可行且成本低；CopilotChat 官方 wiki 推荐"聊天 buffer + render-markdown"，说明该组合已被验证。

## `~/.lwl/nvim/` 目录布局（`--clean` 下经 `runtimepath` 注入）

```
~/.lwl/nvim/
  lua/agent/
    init.lua          组件层入口, 对外暴露 call(event, payload)
    layout.lua        布局加载与区域装配
    handlers/         按组件类型查处理器
    buffer_stream.lua 行缓冲流式写入 (攒批/切块/行上限/禁 undo)
    input.lua         多行输入与提交
  layout.json         UI 布局描述（配置, 非代码）
  syntax/             自带高亮脚本
  after/syntax/       追加用
  plugins/            nui 等社区库落位处
```

上行事件（submit / cancel / tool.approve）经 `vim.rpcnotify` 发往 Java，见 `phase-5-nvim-dist.md`。

## 渲染两条路径

```
对话消息流  → append-only 行缓冲（按行写入递增的行号，只重画当前流式消息的尾部）
工具卡片    → 同一 buffer 内的折叠段（或仅让树组件管理侧栏等小结构）
布局 / 弹层 → 组件状态（nui-components 的 state）
```

依据：`nui` 的树组件是整树重绘（`tree:render()`）。把对话流塞进一棵树、每来一个 token 就整体重绘，长对话必然退化。**行缓冲与组件状态是两条不同的渲染路径，不得混用**。

## 流式写入纪律（来自真实事故，非推测）

1. **不得产生超长行**。`neovim#34019`：单行 80 万字符 + wrap 导致 100% CPU 挂死。因此 token 必须攒批、按换行切块后一次性写入，且单行长度有上限，超出即折行。
2. `undolevels = -1`（官方 `vi_diff`："Set it to -1 for no undo at all"），流式写入不进 undo 树。
3. 避免 legacy regex syntax 在超大 buffer 上的导航开销（`neovim#17862`，`--clean` 下同样复现）；长历史靠折叠收缩而非无界增长。
4. 高频变更触发的自动命令代价高（官方对 `TextChangedT` 的告警），不挂重活。

按既定策略：**默认使用开源组件，出现性能问题再针对性开发**，不设前置探针。

## 代码高亮：加载与开关的具体做法

- syntax 加载走 `runtime! syntax/<name>.vim`，**搜索整个 `runtimepath`** → 把自有高亮脚本放进自有配置目录并注入 `runtimepath` 即可覆盖或补充内置脚本，`after/syntax/` 用于追加。

```vim
" 由 --cmd 或 -c 在启动早期注入
set runtimepath+=~/.lwl/nvim

" 代码块内嵌语言高亮 (内置 markdown syntax 支持)
let g:vimsyn_embed = 'lpch'           " 默认仅 'l' (Lua)
" after/syntax/*.vim 用于在内置脚本之后追加
```

### 否决的路线：预编译 treesitter parser 随包分发

```
nvim 内置 treesitter parser (官方 :h treesitter 原文):
  C  Diff  Lua  Markdown  Vimscript  Vimdoc   (+ markdown_inline, query)

  => render-markdown 的【必需】parser (markdown/markdown_inline) 免费得到
  => python/js/java/go/rust/json/yaml 一个都没有

:TSInstall 的三个强制前置 (多个 parser 管理器一致列出):
  tree-sitter CLI   +   C 编译器 (gcc/clang/cl.exe)   +   git
        |
        └─ nvim-treesitter#8206 就是这个失败:
           "Failed to execute the C compiler command: cl.exe ..."
```

质量虽最好，但需要"平台 × 架构 × 语言"的编译矩阵与包体积代价，且运行时前置与"下载一个 nvim 就能用"的分发模型冲突。
