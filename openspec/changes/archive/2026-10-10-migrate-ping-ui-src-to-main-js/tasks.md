# Tasks

## 1. 源码树迁移（git mv，零内容改动）

- [x] 1.1 用 `git mv` 将 `ping-ui/src/` 根下的 4 个源文件（`main.tsx`、`dev-entry.tsx`、`RendererRoot.tsx`、`vite-env.d.ts`）迁入 `ping-ui/src/main/js/`，验证 `git status --short` 全部显示为 `R`（重命名）且 `src/` 根不再残留 `.ts/.tsx` 文件
- [x] 1.2 用 `git mv` 将 9 个源码子目录（`components`、`layout`、`pages`、`router`、`data`、`lib`、`mock`、`schema`、`styles`）整体迁入 `ping-ui/src/main/js/`，验证 `find ping-ui/src/main/js -type f | wc -l` 等于迁移前基线 38（根 4 + components 17 + data 2 + layout 6 + lib 1 + mock 1 + pages 2 + router 3 + schema 1 + styles 1；任务 1.1 已迁根 4，故本次迁移 34），且各子目录相对结构不变
- [x] 1.3 验证 `ping-ui/src` 一级目录仅剩 `main`、`test`（`ls ping-ui/src`），且 `src/main` 下并列存在 `java`、`js`、`resources` 三个目录

## 2. 配置文件内部路径引用更新（文件位置不动）

- [x] 2.1 修改 `ping-ui/vite.config.ts`：`@` 别名改指 `./src/main/js`、lib 入口改指 `src/main/js/main.tsx`，验证 `grep -n "src" ping-ui/vite.config.ts` 中不再出现 `./src"` 与 `src/main.tsx` 旧写法
- [x] 2.2 修改 `ping-ui/tsconfig.json`：`paths` 的 `@/*` 映射改为 `./src/main/js/*`，验证 `grep -n '"@/\*"' ping-ui/tsconfig.json` 输出为 `["./src/main/js/*"]`，`include` 保持 `["src", "vite.config.ts"]` 不变
- [x] 2.3 修改 `ping-ui/index.html`：模块脚本改为 `/src/main/js/dev-entry.tsx`，验证 `grep -n "dev-entry" ping-ui/index.html` 命中新路径且无 `/src/dev-entry` 旧路径
- [x] 2.4 修改 `ping-ui/components.json`：`tailwind.css` 改为 `src/main/js/styles/globals.css`，验证 `grep -n "globals.css" ping-ui/components.json` 命中新路径，且 `aliases` 段（基于 `@/`）保持不变
- [x] 2.5 全模块 grep 验证无残留旧引用：`grep -rn "src/" ping-ui --include='*.ts' --include='*.tsx' --include='*.json' --include='*.html' | grep -v node_modules | grep -v target | grep -v "src/main/js" | grep -v "src/main/java" | grep -v "src/main/resources" | grep -v "src/test"` 输出为空（`package-lock.json` 内的 `src/cli/...` 为依赖内部路径，允许存在）

## 3. 构建与测试验证

- [x] 3.1 在 `ping-ui` 运行 `npm run build`，验证 `tsc --noEmit` 与 vite build 均成功产出 `target/dist/renderer.js`（别名解析与入口路径正确）
- [x] 3.2 运行 `./mvnw -Ph2 -pl ping-ui test`，验证全部测试通过（含 `StaticAssetsTest` 的主壳/薄壳/模板落位断言，证明 Maven 侧不受迁移影响）
- [x] 3.3 集成抽查：`git status --short ping-ui` 仅包含「源文件重命名 + 4 个配置文件修改」两类变更，`ping-ui/pom.xml`、`package.json`、Java 源码、`src/main/resources` 均无改动

## Workflow follow-up

- 实现完成并通过验证后，运行 `/opsx-verify` 校验实现与本变更制品一致。
- 在项目审查要求满足后归档该变更（`openspec archive`）。
