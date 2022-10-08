package cn.procsl.ping.boot.admin.service;

import cn.procsl.ping.boot.admin.advice.DataPermissionRootAttributeRegistry;
import cn.procsl.ping.boot.admin.auth.login.SessionUserDetail;
import cn.procsl.ping.boot.admin.domain.rbac.AccessControlService;
import cn.procsl.ping.boot.admin.domain.rbac.DataPermission;
import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import cn.procsl.ping.boot.admin.domain.rbac.PermissionCacheService;
import cn.procsl.ping.boot.common.event.Subscriber;
import cn.procsl.ping.boot.common.event.SubscriberRegister;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.function.TriFunction;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static cn.procsl.ping.boot.admin.constant.EventPublisherConstant.*;

@Slf4j
@RequiredArgsConstructor
@SubscriberRegister
public class DataPermissionCacheService implements PermissionCacheService<DataPermission, Long> {

    final AccessControlService accessControlService;
    final CacheManager cacheManager;

    @Override
    public Collection<DataPermission> getPermissions(Long id) {
        Cache cache = this.cacheManager.getCache("DataPermissionCache");
        Collection<DataPermission> permission = cache.get(id, Collection.class);
        if (permission != null) {
            return permission;
        }
        Collection<DataPermission> tmp = this.accessControlService.loadPermissions(id, this::loadPermission);
        cache.put(id, tmp);
        log.info("加载Data权限:{},{}", id, tmp);
        return tmp;
    }

    @Subscriber(name = PERMISSION_UPDATE_EVENT)
    @Subscriber(name = PERMISSION_DELETE_EVENT)
    @Subscriber(name = ROLE_CHANGED_EVENT)
    @Subscriber(name = ROLE_DELETE_EVENT)
    public void reload() {
        Cache cache = this.cacheManager.getCache("DataPermissionCache");
        if (cache != null) {
            cache.clear();
        }
        log.info("重新加载用户权限信息");
    }

    @Subscriber(name = USER_LOGOUT)
    @Subscriber(name = GRANT_CHANGED)
    public void onLogout(Long id) {
        Cache cache = this.cacheManager.getCache("DataPermissionCache");
        if (cache != null) {
            cache.evict(id);
        }
        log.info("卸载用户权限信息:{}", id);
    }

    DataPermission loadPermission(Permission permission) {
        if (permission instanceof DataPermission) {
            log.debug("权限: {}", permission);
            return (DataPermission) permission;
        }
        return null;
    }

    @Indexed
    @Component
    @RequiredArgsConstructor
    static class UserDataPermissionRegistry implements DataPermissionRootAttributeRegistry {

        final PermissionCacheService<DataPermission, Long> permissionLongPermissionCacheService;

        final HttpServletDataPermissionMatcherService matcherService = new HttpServletDataPermissionMatcherService();

        @Override
        public Map<String, Object> getAttributes() {
            Object func = currentUserDataPermission();
            TriFunction<Object[], Object, Integer, Boolean> override =
                    this::overrideArgument;
            return Map.of("currentUserDataPermission", func, "overrideArgument", override);
        }

        // "root[override](argument,arg, 1)"
        boolean overrideArgument(Object[] arguments, Object arg, int index) {
            if (!(arguments.length >= index)) {
                log.warn("参数错误:{}", arguments);
            }
            arguments[index] = arg;
            return true;
        }

        Function<String, Boolean> currentUserDataPermission() {
            return s -> {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null) {
                    return false;
                }
                if (!authentication.isAuthenticated() && authentication.getPrincipal() instanceof SessionUserDetail) {
                    return false;
                }

                SessionUserDetail userInfo = ((SessionUserDetail) authentication.getPrincipal());

                Collection<DataPermission> permission = permissionLongPermissionCacheService.getPermissions(
                        userInfo.getId());
                Collection<DataPermission> res = matcherService.matcher(s, permission);
                return res.isEmpty();
            };
        }
    }

}
