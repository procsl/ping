package cn.procsl.ping.boot.admin.auth.access;

import cn.procsl.ping.boot.admin.domain.rbac.HttpPermission;
import cn.procsl.ping.boot.admin.domain.rbac.Subject;
import cn.procsl.ping.boot.admin.domain.rbac.SubjectRoleSpecification;
import cn.procsl.ping.boot.common.event.Subscriber;
import cn.procsl.ping.boot.common.event.SubscriberRegister;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static cn.procsl.ping.boot.admin.constant.EventPublisherConstant.*;

@Slf4j
@SubscriberRegister
@RequiredArgsConstructor
public class GrantedAuthorityLoader {

    final JpaSpecificationExecutor<Subject> subjectJpaSpecificationExecutor;

    final ConcurrentHashMap<Long, Collection<HttpPermission>> concurrentHashMap = new ConcurrentHashMap<>();

    public Collection<HttpPermission> getPermissions(Long id) {
        Collection<HttpPermission> permissions = this.concurrentHashMap.get(id);
        if (permissions == null) {
            Optional<Subject> optional = subjectJpaSpecificationExecutor.findOne(
                    new SubjectRoleSpecification(id, null));

            if (optional.isEmpty()) {
                return Collections.emptyList();
            }

            Collection<HttpPermission> permission = optional.get().getPermissions(HttpPermission.class);
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

    @Subscriber(name = USER_LOGIN)
    public void logout(Long id) {
        log.info("用户注销,卸载用户权限信息:{}", id);
        concurrentHashMap.remove(id);
    }

}
