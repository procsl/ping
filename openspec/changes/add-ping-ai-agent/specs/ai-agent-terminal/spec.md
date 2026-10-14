# Spec Delta

## Purpose

让同一套 agent 能力以终端单次调用、终端交互式编辑器与 web 三种外壳交付，每种外壳只负责传输与呈现而不复制业务逻辑。

## ADDED Requirements

### Requirement: 非交互式单次问答
非交互式调用 SHALL 接收一个用户提示词，把增量产出写到标准输出，并在完成后退出。

#### Scenario: 正常问答
- **WHEN** 以用户提示词调用非交互式形态
- **THEN** 增量输出到标准输出并以成功状态退出

#### Scenario: 输出接入管道
- **WHEN** 其标准输出接入管道
- **THEN** 输出不包含交互式界面所需的控制序列

### Requirement: 命令行参数风格
命令行参数 SHALL 使用长参数形式，SHALL NOT 要求使用者记忆短参数。

#### Scenario: 指定角色与工具
- **WHEN** 以长参数形式指定角色与工具
- **THEN** 参数被解析并生效

### Requirement: 交互式渲染引擎
交互式形态 SHALL 启动一个 Neovim 实例作为渲染引擎，该实例 SHALL NOT 读取用户的 Neovim 配置，SHALL 保留 Vim 默认按键方式。

#### Scenario: 不受用户配置影响
- **WHEN** 用户机器上存在自己的 Neovim 配置与插件
- **THEN** 启动的实例不加载它们，界面表现与用户配置无关

#### Scenario: Vim 按键可用
- **WHEN** 在交互界面中操作
- **THEN** Vim 标准移动与编辑按键生效

### Requirement: 终端形态不监听端口
终端形态 SHALL NOT 监听网络端口，除非显式要求 web 形态。

#### Scenario: 普通启动
- **WHEN** 未指定 web 形态启动
- **THEN** 进程不建立任何网络监听

### Requirement: 双形态打包
模块被其他模块作为依赖打包时 SHALL 提供 web 服务形态；单独打包时 SHALL 为终端程序，除非显式要求 web。

#### Scenario: 作为依赖打包
- **WHEN** 被其他模块打包引入
- **THEN** 以 web 服务形态提供能力

#### Scenario: 单独打包运行
- **WHEN** 单独作为终端程序运行且未指定 web
- **THEN** 不启动 web 服务

### Requirement: Neovim 自动获取
系统 SHALL 在需要时自行获取 Neovim 二进制到约定目录，SHALL NOT 要求系统预装。

#### Scenario: 首次使用
- **WHEN** 本机没有安装 Neovim
- **THEN** 系统获取到约定目录并用它启动交互界面

### Requirement: 图片输入与降级展示
图片 SHALL 能作为输入交给模型；当渲染端不具备图片显示能力时 SHALL 显示该图片的文件名。

#### Scenario: 传入图片
- **WHEN** 用户粘贴或引用了一个图片文件
- **THEN** 图片内容作为模型输入的一部分被传递

#### Scenario: 无法显示
- **WHEN** 渲染端不支持图片显示
- **THEN** 界面显示该图片的文件名

### Requirement: Web 形态消息语义一致
web 形态与终端交互式形态 SHALL 使用同一套消息语义，差异仅限于传输方式。

#### Scenario: 同一事件两种传输
- **WHEN** 同一次对话分别在两种形态下运行
- **THEN** 两者表达的事件类型与含义一致
