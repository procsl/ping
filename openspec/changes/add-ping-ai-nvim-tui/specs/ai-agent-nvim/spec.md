# Spec Delta

## Purpose

用 Neovim 作为交互式渲染引擎交付会话：不依赖用户已安装的编辑器与配置，在真实终端里提供流式对话、工具确认与图片降级展示。

## ADDED Requirements

### Requirement: 交互式渲染引擎
交互式形态 SHALL 启动一个 Neovim 实例作为渲染引擎，该实例 SHALL NOT 读取用户的 Neovim 配置，SHALL 保留 Vim 默认按键方式。

#### Scenario: 不受用户配置影响
- **WHEN** 用户机器上存在自己的 Neovim 配置与插件
- **THEN** 启动的实例不加载它们，界面表现与用户配置无关

#### Scenario: Vim 按键可用
- **WHEN** 在交互界面中操作
- **THEN** Vim 标准移动与编辑按键生效

### Requirement: Neovim 自动获取
系统 SHALL 在需要时自行获取 Neovim 二进制到约定目录，SHALL NOT 要求系统预装。

#### Scenario: 首次使用
- **WHEN** 本机没有安装 Neovim
- **THEN** 系统获取到约定目录并用它启动交互界面

#### Scenario: 获取失败
- **WHEN** 无法下载、校验和不匹配或落位失败
- **THEN** 系统输出包含原因的错误，并提示可改用非交互形态，SHALL NOT 静默失败或假装已降级

### Requirement: 流式呈现不退化
交互界面 SHALL 在高频增量下保持可用：不得产生超长行、不得让渲染回调随增量次数线性增长、不得阻塞输入。

#### Scenario: 长对话流式输出
- **WHEN** 一次回答产生大量增量片段
- **THEN** 界面持续可交互，行长度受控，渲染开销不随片段数累积

#### Scenario: markdown 渲染时机
- **WHEN** 一条消息正在流式产出
- **THEN** markdown 渲染不在每个增量片段上重复触发，而是在消息完成后或冻结区间内渲染

### Requirement: 工具确认呈现
对白名单之外的工具调用，交互形态 SHALL 在界面请求人工确认，确认之前 SHALL NOT 执行。

#### Scenario: 确认请求
- **WHEN** 模型请求调用白名单之外的工具
- **THEN** 界面呈现确认请求，工具在确认前不执行

#### Scenario: 确认结果
- **WHEN** 用户允许或拒绝
- **THEN** 允许则执行并继续对话，拒绝则把拒绝结果反馈给模型与界面

### Requirement: 图片无法显示时降级
当渲染端不具备图片显示能力时，界面 SHALL 显示该图片的文件名。

#### Scenario: 无法显示
- **WHEN** 消息中包含图片且渲染端不支持图片显示
- **THEN** 界面显示该图片的文件名，不报错也不留空白

### Requirement: 退出收尾
Neovim 进程退出时（正常或异常），系统 SHALL 停止 box 流并完成收尾。

#### Scenario: 用户退出界面
- **WHEN** 用户退出 Neovim
- **THEN** box 流停止、进程结束、会话内容已落盘
