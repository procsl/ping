package cn.procsl.ping.boot.admin.web.config.security;

import cn.procsl.ping.boot.admin.domain.rbac.HttpPermission;
import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import cn.procsl.ping.boot.admin.service.PermissionMatcher;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

class HttpPermissionMatcher implements PermissionMatcher {

    final static PathPatternParser parser = PathPatternParser.defaultInstance;
    final Map<String, List<HttpPermission>> permissions;

    public HttpPermissionMatcher(@NonNull Collection<HttpPermission> permissions) {
        this.permissions = permissions.stream().collect(Collectors.groupingBy(Permission::getOperate));
    }


    @Override
    public Optional<Permission> matcher(HttpServletRequest request) {

        String method = request.getMethod();
        if (this.permissions.isEmpty() || (!this.permissions.containsKey(method))) {
            return Optional.empty();
        }

        Function<Permission, Boolean> wrapper = permission -> {
            String currentUrl = request.getRequestURI();
            if (permission.matcher(request.getMethod(), currentUrl)) {
                return true;
            }

            return dynamicMatcher((HttpPermission) permission, currentUrl);
        };

        val urls = this.permissions.get(method);
        for (HttpPermission url : urls) {
            if (url.matcher(wrapper)) {
                return Optional.of(url);
            }
        }
        return Optional.empty();
    }

    boolean dynamicMatcher(HttpPermission permission, String currentUrl) {
        if (permission.isDynamicURI()) {
            return false;
        }

        PathPattern pattern = parser.parse(permission.getUrl());
        PathContainer container = PathContainer.parsePath(currentUrl, PathContainer.Options.HTTP_PATH);
        return pattern.matches(container);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
    }


}
