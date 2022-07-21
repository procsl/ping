package cn.procsl.ping.boot.admin.service;

import cn.procsl.ping.boot.admin.domain.rbac.Permission;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface PermissionMatcher {

    Optional<Permission> matcher(HttpServletRequest request);

}
