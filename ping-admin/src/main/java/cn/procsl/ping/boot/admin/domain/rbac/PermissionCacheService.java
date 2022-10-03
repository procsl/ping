package cn.procsl.ping.boot.admin.domain.rbac;

import java.io.Serializable;
import java.util.Collection;

public interface PermissionCacheService<T extends Permission, K extends Serializable> {

    Collection<T> getPermissions(K key);
}
