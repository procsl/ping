package cn.procsl.ping.boot.user.facade;

import cn.procsl.ping.boot.data.business.BusinessException;

import javax.transaction.Transactional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

import static cn.procsl.ping.boot.user.domain.rbac.entity.Role.NAME_LENGTH;

/**
 * rbac 2服务接口, 增加角色约束
 *
 * @author procsl
 * @date 2020/06/24
 */
public interface ConstraintRoleFacade extends RoleFacade {

    /**
     * 添加静态前置角色约束, 将此角色赋予指定的身份前必须拥有前置角色
     *
     * @param roleId          待添加的角色ID
     * @param requiredRoleIds 约束的角色IDs
     * @throws BusinessException 如果前置角色错误或者冲突,则抛出此异常
     */
    @Transactional(rollbackOn = Exception.class)
    void addRequire(@NotNull Long roleId, @NotEmpty Set<Long> requiredRoleIds) throws BusinessException;

    /**
     * 删除前置角色约束
     *
     * @param roleId          被操作的角色
     * @param requiredRoleIds 被删除的前置约束角色IDs
     * @throws BusinessException 如果该前置约束服务被删除,则抛出异常
     */
    @Transactional(rollbackOn = Exception.class)
    void removeRequire(@NotNull Long roleId, @NotEmpty Set<Long> requiredRoleIds) throws BusinessException;

    /**
     * 设置最大角色约束
     * 表示该角色最大被直接引用的数量, 不包括继承的
     *
     * @param roleId 被操作的角色ID
     * @param max    最大数量, 小于0, 表示无约束
     * @throws BusinessException 如果约束数量不合法, 则抛出异常
     */
    @Transactional(rollbackOn = Exception.class)
    void setMax(@NotNull Long roleId, int max) throws BusinessException;

    /**
     * 添加互斥角色
     *
     * @param roleId         待操作的角色
     * @param excludeRoleIds 互斥的角色
     * @throws BusinessException 业务异常
     */
    @Transactional(rollbackOn = Exception.class)
    void addExcludes(@NotNull Long roleId, @NotEmpty Set<Long> excludeRoleIds) throws BusinessException;

    /**
     * 移除互斥角色
     *
     * @param roleId        待操作的角色ID
     * @param excludeRoleId 互斥角色ID
     * @throws BusinessException 业务异常
     */
    @Transactional(rollbackOn = Exception.class)
    void removeExclude(@NotNull Long roleId, @NotNull Long excludeRoleId) throws BusinessException;

    /**
     * 覆盖指定角色的属性
     *
     * @param roleId          指定的角色ID
     * @param name            角色名称, 如果不为null, 则覆盖为此值
     * @param inheritRoleId   继承角色IDs, 如果不为null, 则覆盖为此值
     * @param max             角色最大限制数量, 如果不为null,则覆盖为此值
     * @param requiredRoleIds 前置角色IDs, 如果不为null, 则覆盖之
     * @param excludeRoleIds  互斥角色IDs, 如果不为null, 则覆盖之
     * @param resourceIds     关联的资源IDs, 如果不为null, 则覆盖之
     */
    void override(@NotNull Long roleId, @Size(min = 1, max = NAME_LENGTH) String name,
                  Long inheritRoleId, int max, Set<Long> requiredRoleIds, Set<Long> excludeRoleIds, Set<Long> resourceIds);
}

