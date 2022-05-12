package cn.procsl.ping.boot.user;

import javax.validation.constraints.NotNull;
import java.util.Collection;

public interface AccessControlFacade {
    void grant(@NotNull Long userId, @NotNull Collection<String> defaultRegisterRoleNames);

    void grantDefaultRoles(@NotNull Long userId);

    Collection<String> getDefaultRoles();

    void addDefaultRoles(@NotNull Collection<String> value);


}
