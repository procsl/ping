package cn.procsl.ping.boot.infra.user;

import javax.validation.constraints.NotNull;
import java.util.Collection;

public interface AccessControlFacade {

    /**
     * 访问控制模块门面 授权用户角色
     *
     * @param userId    用户ID
     * @param roleNames 角色名称
     */
    void grant(@NotNull Long userId, @NotNull Collection<String> roleNames);

    /**
     * 访问控制模块, 授权系统中设置的默认权限
     *
     * @param userId 用户ID
     */
    void grantDefaultRoles(@NotNull Long userId);

    /**
     * @return 获取系统中设置的用户注册默认角色
     */
    Collection<String> getDefaultRoles();

    /**
     * 设置系统中默认的用户角色
     *
     * @param roles 角色名称
     */
    void defaultRoleSetting(@NotNull Collection<String> roles);


}
