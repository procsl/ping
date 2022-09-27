package cn.procsl.ping.boot.admin.domain.rbac;

import lombok.NonNull;

import java.util.Collection;

public interface PermissionMatcherService<P extends Permission> {

    Collection<P> matcher(@NonNull Object context, @NonNull Collection<P> permissions);

}
