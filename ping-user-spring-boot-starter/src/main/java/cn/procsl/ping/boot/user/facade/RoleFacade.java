package cn.procsl.ping.boot.user.facade;

import cn.procsl.ping.boot.data.business.BusinessException;

import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
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
public interface RoleFacade {

    /**
     * 创建角色
     *
     * @param name          角色名称
     * @param inheritRoleId 继承的角色
     * @return 返回角色ID
     * @throws BusinessException 如果角儿创建失败,例如角色已经存在 抛出此异常
     */
    @Transactional(rollbackOn = Exception.class)
    Long create(@NotBlank @Size(min = 1, max = NAME_LENGTH) String name, Long inheritRoleId) throws BusinessException;


    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @throws BusinessException 如果角色被绑定, 则不可删除
     */
    @Transactional(rollbackOn = Exception.class)
    void delete(@NotNull Long roleId) throws BusinessException;


    /**
     * 角色重命名
     *
     * @param roleId 指定的角色
     * @param name   角色新名称
     * @throws BusinessException 如果该名称被占用, 则抛出异常
     */
    @Transactional(rollbackOn = Exception.class)
    void rename(@NotNull Long roleId, @NotBlank @Size(min = 1, max = NAME_LENGTH) String name) throws BusinessException;

    /**
     * 修改继承角色
     *
     * @param roleId        带操作的角色
     * @param inheritRoleId 继承的角色
     * @throws BusinessException 业务异常
     */
    @Transactional(rollbackOn = Exception.class)
    void changeInherit(@NotNull Long roleId, @NotNull Long inheritRoleId) throws BusinessException;


    /**
     * 为指定的角色添加权限
     *
     * @param roleId     角色ID
     * @param resourceId 资源ID
     * @throws BusinessException 如果添加失败,则抛出此异常
     */
    @Transactional(rollbackOn = Exception.class)
    void addPermission(@NotNull Long roleId, @NotNull Long resourceId) throws BusinessException;

    /**
     * 删除权限
     *
     * @param roleId     指定的角色
     * @param resourceId 待删除的资源ID
     * @throws BusinessException 如果该资源不支持从该角色中删除, 则抛出异常
     */
    @Transactional(rollbackOn = Exception.class)
    void removePermission(@NotNull Long roleId, @NotNull Long resourceId) throws BusinessException;


    /**
     * 覆盖指定角色的属性
     *
     * @param roleId        指定的角色ID
     * @param name          角色名称, 如果不为null, 则覆盖为此值
     * @param inheritRoleId 继承角色IDs, 如果不为null, 则覆盖为此值
     * @param resourceId    关联的资源IDs, 如果不为null, 则覆盖之
     * @throws BusinessException 业务异常
     */
    @Transactional(rollbackOn = Exception.class)
    void override(@NotNull Long roleId, @Size(min = 1, max = NAME_LENGTH) String name, Long inheritRoleId, Set<Long> resourceId) throws BusinessException;


    /**
     * 检测指定的角色是否具有指定的权限(资源)
     *
     * @param roleId     指定的角色ID
     * @param resourceId 指定的资源ID
     * @return 返回是否具有此权限
     */
    boolean hasPermission(@NotNull Long roleId, @NotNull Long resourceId);


    /**
     * 获取指定角色的所有权限
     *
     * @param roleId 指定的角色
     * @return 返回权限列表
     */
    Set<Long> getPermissions(@NotNull Long roleId);
}
