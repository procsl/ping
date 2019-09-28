## ping开发文档

ping只实现基础的路由功能,其他由插件完成处理

Master 核心功能
1.  获取当前生效的配置列表

        GET /configs
    
2.  添加一份配

        POST /config
       
3.  修改一份配置部分属

        PATCH /configs/{ID}
    
4.  修改一份配置的所有属性

        PUT /configs/{ID}
        
5.  发布配置

        PATCH /configs/{ID1}
        {
            "status":"published"
        }
    
6.  查询任务实例列表

        GET /workers
    
7.  创建一个任务实例,默认创建的是未启用的,使用指定的的配置及插件
对于配置需要已经发布的

        POST /workers
        {
            "configs":[...],
            "plugins":[...],
        }
    
8.  重新加载任务

        PATCH /workers/{ID1}:{ID2}....
        {
            "status":"reload"
        }
    
9.  停止指定的工作线程

        PATCH /workers/{ID1}:{ID2}....
        {
            "status":"stop"
        }
    
    
    
