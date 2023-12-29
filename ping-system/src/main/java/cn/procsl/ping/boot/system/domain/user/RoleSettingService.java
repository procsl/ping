package cn.procsl.ping.boot.system.domain.user;

import jakarta.validation.constraints.NotNull;

import java.util.Collection;

public interface RoleSettingService {


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
