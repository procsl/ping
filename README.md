# PING

### PING - 基于事件循环的轻量级网关
1. ##### PING是什么?
    一个使用JAVA语言的、基于Vert.x事件和异步框架作为\
    底层架构的应用级HTTP代理服务器。
    
2. ##### PING有哪些功能?
    HTTP反向代理
    
3. ##### PING架构?
    PING使用Master+Worker多线程的工作模式\
    Master为主线程,有且只有一个\
    Worker线程唯一对应上下文实例,防止线程安全问题\
    Worker线程数默认为cpu核心数的2倍\
    Worker可被安全的注销和重新生成,由Master管理\

4. ##### PING的扩展性
    ping是高度可扩展的.
    基于JAVA提供的SPI功能

5. ##### ping的插件
    * 用户认证插件
    * 服务路由插件
    * 服务发现插件
    * 配置动态插件
    * 控制面板插件
    * 静态资源插件
    