package cn.procsl.ping.boot.admin.domain.rbac;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
public class HttpServletPermissionMatcherService implements
        PermissionMatcherService<HttpPermission> {

    @Override
    public Collection<HttpPermission> matcher(@NonNull Object context,
                                              @NonNull Collection<HttpPermission> permissions) {
        if (!(context instanceof HttpServletRequest)) {
            throw new UnsupportedOperationException("不支持的上下文");
        }
        HttpServletRequest request = (HttpServletRequest) context;
        String method = request.getMethod();
        String url = request.getRequestURI();
        return permissions.stream()
                          .filter(item -> item.matcher(method, url))
                          .peek(item -> log.debug("[{}:{}]匹配到权限:[{}]", method, url, item))
                          .collect(Collectors.toList());
    }

}
