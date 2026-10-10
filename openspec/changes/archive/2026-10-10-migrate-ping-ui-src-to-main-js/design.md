# Design

## Context

动机参见 proposal.md - Why。现状要点（已审计确认）：

- `ping-ui/src` 下前端源码 38 个文件（`.ts`/`.tsx`/`.css`；基线实测：根 4 + components 17 + data 2 + layout 6 + lib 1 + mock 1 + pages 2 + router 3 + schema 1 + styles 1），与 `src/main/java`、`src/main/resources`、`src/test` 混布
- **全部 32 个含 import 的源文件一律使用 `@/` 别名**，仓内 0 处相对路径 import —— 源文件内容与目录结构解耦，迁移只需改别名的解析位置
- 指向 `src/` 源码路径的引用仅存在于 4 个配置文件（审计全仓 `*.md|json|ts|tsx|html|xml|properties` 确认）：
  - `index.html:10` → `/src/dev-entry.tsx`
  - `vite.config.ts` → `@` 别名（`./src`）与 lib 入口（`src/main.tsx`）
  - `tsconfig.json` → `paths` 的 `@/*` 映射（`./src/*`）
  - `components.json` → `tailwind.css`（`src/styles/globals.css`）
- 约束：`ping-ui/pom.xml` 构建只读 `src/main/java`（编译）与 `src/main/resources`（资源），`npm run build` 工作目录是模块根，均不感知 `src/` 下的前端文件位置；运行时产物落位 `META-INF/resources/`，与源码树无关

## Goals / Non-Goals

**Goals:**

- `src/` 树对齐 Java 项目格式：迁移后仅含 `main/js`、`main/java`、`main/resources`、`test`
- 迁移为纯移动：源文件内容零改动，构建产物与运行时行为逐字节等价
- 配置文件留在模块根，仅更新其内部指向源码的路径引用

**Non-Goals:**

- 不改 `package.json`、`pom.xml`、构建脚本、依赖版本
- 不重命名文件/目录、不调整 `src/main/js` 内部结构、不顺手重构源码或改 import 风格
- 不迁移到新的打包工具或目录约定（如 `src/main/frontend`、monorepo）
- 不改 `ping-ui/doc/*.md`（审计确认其无 `src/` 路径引用）

## Decisions

**D1：整树 `git mv` 迁移，而非复制重建**
子目录结构在 `src/main/js` 下原样保留。用 `git mv` 保留文件历史，`git status` 一眼验证无遗漏。
备选：逐文件复制后删除 —— 丢历史、易漏文件，否决。

**D2：源文件零内容改动，别名在配置层重指**
因全部 import 走 `@/`，只需把 `tsconfig.json` `paths` 与 `vite.config.ts` alias 从 `./src` 改指 `./src/main/js`，所有源文件 import 原样成立。
备选：改配置为 `./src` 不动、靠子路径兼容 —— 违背「对齐 Java 格式」目标，`src/` 根仍残留源码，否决。

**D3：`src/main/js` 不接入 Maven 流水线**
Maven 不编译/打包该目录（`sourceDirectory` 默认 `src/main/java`，resources 显式声明 `src/main/resources`），前端仍由根目录 `npm run build` 经 `exec-maven-plugin` 驱动。`pom.xml` 零改动。
备选：把 js 目录纳入 pom 资源/插件配置 —— 无收益且引入耦合，否决。

**D4：迁移与配置更新分两步提交点**
先 `git mv` + 配置路径更新，再立即跑构建验证；任一验证失败可整体 `git checkout` 回滚，工作树干净。
备选：迁移与验证之间穿插其他改动 —— 回滚粒度变粗，否决。

## Risks / Trade-offs

- [`tsc --noEmit`/vite 因遗漏引用失败] → 迁移前已完成全仓引用审计，仅 4 个配置文件；迁移后依次跑 `npm run build` 与 mvn 测试双闸门
- [shadcn CLI 未来生成组件落到旧路径] → `components.json` 的 `css` 与 `aliases` 基于 `@/`，D2 更新别名后自动落位 `src/main/js/components/ui`
- [IDE/TS server 缓存旧路径] → 迁移后重启 TypeScript server（一次性、无代码影响）
- [`tsconfig include: ["src"]` 范围过宽] → 迁移前后语义一致（本来就覆盖整个 `src/`），无回归；保持不动以符合「配置文件位置与其余配置不改」
- [他人本地存在未提交改动冲突] → 迁移仅动 `ping-ui`，当前 `git status` 中该模块无未提交修改，合并前 rebase 即可

## Migration Plan

1. `git mv` 迁移全部前端源码与 `styles/` 至 `src/main/js/`
2. 更新 4 个配置文件中的路径引用（位点见 Context）
3. 验证：`npm run build`（tsc + vite）→ `./mvnw -Ph2 -pl ping-ui test`（含 `StaticAssetsTest`）
4. 回滚策略：任何一步失败，`git checkout -- ping-ui` 恢复，无数据/状态迁移，无部署耦合
