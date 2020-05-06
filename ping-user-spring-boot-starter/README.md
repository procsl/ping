# 用户模块
## 用户管理

#基于RBAC的权限管理系统
## 身份管理
> 用户身份实体是关联具体的组织实体或者用户实体的中间实体，用于操作当前身份的角色
>用户身份可以用于用户组，组织，用户等等具有角色权限管理概念的实体。

### 用户身份模块要求
创建用户身份实体 必须指定一个身份类型，一个身份ID。
创建成功之后, 用户身份实体的身份类型以及身份ID不可修改

### 用户身份导出接口:
* 创建用户身份信息
使用身份类型, 身份ID创建一个身份标识对象,如果已经存在该对象, 则抛出异常。
创建成功则返回唯一的用户身份实体 \
`Identity IdentityService.create(String type, String ID) throws BusinessException`\
创建成功后释出创建用户身份信息事件

* 查找用户身份 通过ID\
`Identity IdentityService.findById(Long identityId)`

*  删除用户身份
该操作将会实际删除用户的身份信息, 包括关联的角色信息, 但不包括角色实体等\
`void IdentityService.delete(Long identityId)`\
删除成功过后释出删除用户身份信息事件

* 为当前的身份添加角色\
`void Identity.addRoles(List<Long> roleId)`

* 为当前的身份删除角色\
`void Identity.removeRoles(List<Long> roleId)`

* 刷新事件, 发出当前用户身份实体修改的事件, 用于添加删除角色之后\
`void IdentityService.refresh(Long id)`

**其他的所有的业务逻辑都基于此基础接口**

## 角色管理
> 角色可以不具有任何的权限,但必须具有一个唯一的角色名称

## 角色管理导出接口

* 注册角色信息
该接口用于向系统注册角色相关的信息, 表示创建角色。
必须提供一个在系统中不存在的角色名称, 如果角色已经存在, 则抛出异常\
`Role RoleService.create(String roleName) throws BusinessException`

* 查找角色信息
    1. 通过Id查找\
    `Role RoleService.findById(Long roleName)`

    2. 通过name查找\
    `Role RoleService.findByName(String roleName)`
    
* 删除角色信息\
 如果角色在使用中, 则抛出异常\
 `void RoleService.delete(Long id) throws BusinessException`
 
* 为指定的角色添加权限\
`void Role.addPerm(Long permId)`

* 为指定的角色移除权限\
`void Role.removePerm(Long permId)`

## 权限管理

## 资源管理

## 管理操作接口导出