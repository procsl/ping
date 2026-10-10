# Proposal

## Why

ping-ui 的前端源码（`.ts` / `.tsx` / `.css`）直接散落在 `src/` 根目录，与 Maven 标准布局（`src/main/java`、`src/main/resources`、`src/test/java`）混杂在一起，不符合 Java 项目的目录格式约定，也难以一眼区分「前端源码」「后端源码」「资源与测试」。现在将其规整为 `src/main/js/`，让 `src/` 树整体对齐 Java 项目格式。

## What Changes

- 将 `ping-ui/src` 下全部前端源码迁移到 `src/main/js/`，保持既有子目录结构不变：
  - `src/*.tsx` / `src/*.ts`（入口 `main.tsx`、`dev-entry.tsx`、`RendererRoot.tsx`、`vite-env.d.ts`）
  - `src/components/`、`src/layout/`、`src/pages/`、`src/router/`、`src/data/`、`src/lib/`、`src/mock/`、`src/schema/`
  - `src/styles/globals.css` 一并迁入 `src/main/js/styles/`（经确认：非 JS 源文件随源码树迁移，迁移后 `src/` 根下仅剩 `main/js`、`main/java`、`main/resources`、`test`）
- 模块根目录的前端配置文件**位置不动**，仅同步其中指向源码的路径引用（经确认允许更新内容）：
  - `index.html`：`/src/dev-entry.tsx` → `/src/main/js/dev-entry.tsx`
  - `vite.config.ts`：`@` 别名与 lib 入口指向 `src/main/js`
  - `tsconfig.json`：`paths` 的 `@/*` 映射改为 `./src/main/js/*`
  - `components.json`：`tailwind.css` 路径改为 `src/main/js/styles/globals.css`
- **不改动**：`package.json`、`package-lock.json`、`pom.xml`、`components.json`/`vite.config.ts`/`tsconfig.json` 的位置与其余配置、Java 源码与资源、构建产物与运行时行为
- 无破坏性变更：构建产物（`META-INF/resources/assets/renderer`）、薄壳入口、API 与页面行为完全不变（无 **BREAKING**）

## Capabilities

### New Capabilities

（无）本次为纯源码布局重构，不引入任何系统行为变化；变更的 `.openspec.yaml` 已声明 `skip_specs: true`。

### Modified Capabilities

（无）`openspec/specs/` 当前为空，且无需求层面的变化。

## Impact

- **受影响代码**：仅 `ping-ui` 模块的前端源码树位置，以及根目录 4 个配置文件中的路径引用（`index.html`、`vite.config.ts`、`tsconfig.json`、`components.json`）
- **不受影响**：运行时 API、打包产物、`ping-ui/pom.xml` 构建流程（`npm run build` 工作目录不变）、其他模块、`./mvnw -Ph2 test`
- **验证**：`npm run build`（`tsc --noEmit` + vite build）与 `./mvnw -Ph2 -pl ping-ui test`（含 `StaticAssetsTest`）在迁移前后均通过
