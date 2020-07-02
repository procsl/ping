package cn.procsl.ping.boot.user.service;

import cn.procsl.ping.boot.data.business.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

import static cn.procsl.ping.boot.user.domain.rbac.entity.Role.NAME_LENGTH;

/**
 * 角色 服务接口
 * 实现基本的rbac1 基础功能, 能够实现角色的继承
 *
 * @author procsl
 * @date 2020/06/24
 */
@Named
@Singleton
@RequiredArgsConstructor
@Validated
@Transactional(rollbackFor = Exception.class)
public class RoleService {

    /**
     * 创建角色
     *
     * @param name          角色名称
     * @param inheritRoleId 继承的角色
     * @return 返回角色ID
     * @throws BusinessException 如果角儿创建失败,例如角色已经存在 抛出此异常
     */
    public Long create(@NotBlank @Size(min = 1, max = NAME_LENGTH) String name, Long inheritRoleId) throws BusinessException {

        return null;
    }


    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @throws BusinessException 如果角色被绑定, 则不可删除
     */
    public void delete(@NotNull Long roleId) throws BusinessException {
    }


    /**
     * 角色重命名
     *
     * @param roleId 指定的角色
     * @param name   角色新名称
     * @throws BusinessException 如果该名称被占用, 则抛出异常
     */
    public void rename(@NotNull Long roleId, @NotBlank @Size(min = 1, max = NAME_LENGTH) String name) throws BusinessException {


    }

    /**
     * 修改继承角色
     *
     * @param roleId        带操作的角色
     * @param inheritRoleId 继承的角色
     * @throws BusinessException 业务异常
     */
    public void changeInherit(@NotNull Long roleId, @NotNull Long inheritRoleId) throws BusinessException {

    }


    /**
     * 为指定的角色添加权限
     *
     * @param roleId     角色ID
     * @param resourceId 资源ID
     * @throws BusinessException 如果添加失败,则抛出此异常
     */
    public void addPermission(@NotNull Long roleId, @NotNull Long resourceId) throws BusinessException {

    }

    /**
     * 删除权限
     *
     * @param roleId     指定的角色
     * @param resourceId 待删除的资源ID
     * @throws BusinessException 如果该资源不支持从该角色中删除, 则抛出异常
     */
    public void removePermission(@NotNull Long roleId, @NotNull Long resourceId) throws BusinessException {

    }

    /**
     * 添加静态前置角色约束, 将此角色赋予指定的身份前必须拥有前置角色
     *
     * @param roleId          待添加的角色ID
     * @param requiredRoleIds 约束的角色IDs
     * @throws BusinessException 如果前置角色错误或者冲突,则抛出此异常
     */
    public void addRequire(@NotNull Long roleId, @NotEmpty Set<Long> requiredRoleIds) throws BusinessException {

    }

    /**
     * 删除前置角色约束
     *
     * @param roleId          被操作的角色
     * @param requiredRoleIds 被删除的前置约束角色IDs
     * @throws BusinessException 如果该前置约束服务被删除,则抛出异常
     */
    public void removeRequire(@NotNull Long roleId, @NotEmpty Set<Long> requiredRoleIds) throws BusinessException {

    }

    /**
     * 设置最大角色约束
     * 表示该角色最大被直接引用的数量, 不包括继承的
     *
     * @param roleId 被操作的角色ID
     * @param max    最大数量, 小于0, 表示无约束
     * @throws BusinessException 如果约束数量不合法, 则抛出异常
     */
    public void setMax(@NotNull Long roleId, int max) throws BusinessException {

    }

    /**
     * 添加互斥角色
     *
     * @param roleId         待操作的角色
     * @param excludeRoleIds 互斥的角色
     * @throws BusinessException 业务异常
     */
    public void addExcludes(@NotNull Long roleId, @NotEmpty Set<Long> excludeRoleIds) throws BusinessException {

    }

    /**
     * 移除互斥角色
     *
     * @param roleId        待操作的角色ID
     * @param excludeRoleId 互斥角色ID
     * @throws BusinessException 业务异常
     */
    public void removeExclude(@NotNull Long roleId, @NotNull Long excludeRoleId) throws BusinessException {
    }

    /**
     * 检测指定的角色是否具有指定的权限(资源)
     *
     * @param roleId     指定的角色ID
     * @param resourceId 指定的资源ID
     * @return 返回是否具有此权限
     */
    public boolean hasPermission(@NotNull Long roleId, @NotNull Long resourceId) {
        return false;
    }


    /**
     * 获取指定角色的所有权限
     *
     * @param roleId 指定的角色
     * @return 返回权限列表
     */
    public Set<Long> getPermissions(@NotNull Long roleId) {
        return null;
    }


}
