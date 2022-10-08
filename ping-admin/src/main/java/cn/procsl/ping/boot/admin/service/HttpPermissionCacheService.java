package cn.procsl.ping.boot.admin.service;

import cn.procsl.ping.boot.admin.domain.rbac.AccessControlService;
import cn.procsl.ping.boot.admin.domain.rbac.HttpPermission;
import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import cn.procsl.ping.boot.admin.domain.rbac.PermissionCacheService;
import cn.procsl.ping.boot.common.event.Subscriber;
import cn.procsl.ping.boot.common.event.SubscriberRegister;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;

import java.util.Collection;

import static cn.procsl.ping.boot.admin.constant.EventPublisherConstant.*;

@Slf4j
@SubscriberRegister
@RequiredArgsConstructor
public class HttpPermissionCacheService implements
        PermissionCacheService<HttpPermission, Long> {

    final AccessControlService accessControlService;

    final CacheManager cacheManager;

    @Override
    public Collection<HttpPermission> getPermissions(Long id) {
        Cache cache = this.cacheManager.getCache("HttpPermissionCache");
        Collection<HttpPermission> permission = cache.get(id, Collection.class);
        if (permission != null) {
            return permission;
        }
        Collection<HttpPermission> tmp = this.accessControlService.loadPermissions(id, this::loadPermission);
        cache.put(id, tmp);
        log.info("加载Http权限:{},{}", id, tmp);
        return tmp;
    }

    @Subscriber(name = PERMISSION_UPDATE_EVENT)
    @Subscriber(name = PERMISSION_DELETE_EVENT)
    @Subscriber(name = ROLE_CHANGED_EVENT)
    @Subscriber(name = ROLE_DELETE_EVENT)
    @CacheEvict(cacheNames = "HttpPermissionCache", allEntries = true)
    public void reload() {
        Cache cache = this.cacheManager.getCache("HttpPermissionCache");
        if (cache != null) {
            cache.clear();
        }
        log.info("重新加载用户权限信息");
    }

    @Subscriber(name = USER_LOGOUT)
    @Subscriber(name = GRANT_CHANGED)
    @CacheEvict(cacheNames = "HttpPermissionCache", key = "#id")
    public void onLogout(Long id) {
        Cache cache = this.cacheManager.getCache("HttpPermissionCache");
        if (cache != null) {
            cache.evict(id);
        }
        log.info("卸载用户权限信息:{}", id);
    }

    HttpPermission loadPermission(Permission permission) {
        if (permission instanceof HttpPermission) {
            log.debug("权限: {}", permission);
            return (HttpPermission) permission;
        }
        return null;
    }

}
