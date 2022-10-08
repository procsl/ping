package cn.procsl.ping.boot.admin.domain.rbac;

import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;

public interface PermissionCacheService<T extends Permission, K extends Serializable> {

    @Transactional(readOnly = true)
    Collection<T> getPermissions(K key);
}
