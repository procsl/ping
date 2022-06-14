package cn.procsl.ping.admin.service;

import cn.procsl.ping.boot.infra.domain.rbac.Permission;
import org.springframework.context.ApplicationContextAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface PermissionMatcher extends ApplicationContextAware {

    Optional<Permission> matcher(HttpServletRequest request);

}
