package cn.procsl.ping.boot.admin.service;

import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import org.springframework.context.ApplicationContextAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface PermissionMatcher extends ApplicationContextAware {

    Optional<Permission> matcher(HttpServletRequest request);

}
