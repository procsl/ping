package cn.procsl.ping.boot.admin.auth.access;

import cn.procsl.ping.boot.admin.domain.rbac.AccessControlService;
import cn.procsl.ping.boot.admin.domain.rbac.HttpPermission;
import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import cn.procsl.ping.boot.common.event.Subscriber;
import cn.procsl.ping.boot.common.event.SubscriberRegister;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import static cn.procsl.ping.boot.admin.constant.EventPublisherConstant.*;

@Slf4j
@SubscriberRegister
@RequiredArgsConstructor
public class GrantedAuthorityLoader {

    final AccessControlService accessControlService;

    final ConcurrentHashMap<Long, Collection<HttpPermission>> concurrentHashMap = new ConcurrentHashMap<>();

    public Collection<HttpPermission> getPermissions(Long id) {
        Collection<HttpPermission> permissions = this.concurrentHashMap.get(id);
        if (permissions == null) {
            Collection<HttpPermission> permission = this.accessControlService.loadPermissions(id, this::loadPermission);
            concurrentHashMap.put(id, permission);
            return permission;
        }
        return permissions;
    }

    @Subscriber(name = PERMISSION_UPDATE_EVENT)
    @Subscriber(name = PERMISSION_DELETE_EVENT)
    @Subscriber(name = ROLE_CHANGED_EVENT)
    @Subscriber(name = ROLE_DELETE_EVENT)
    public void reload() {
        log.info("重新加载用户权限信息");
        concurrentHashMap.clear();
    }

    HttpPermission loadPermission(Permission permission) {
        if (permission instanceof HttpPermission) {
            return (HttpPermission) permission;
        }
        return null;
    }

}
