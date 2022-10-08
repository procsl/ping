package cn.procsl.ping.boot.admin.service;

import cn.procsl.ping.boot.admin.domain.rbac.DataPermission;
import cn.procsl.ping.boot.admin.domain.rbac.PermissionMatcherService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
public class HttpServletDataPermissionMatcherService implements PermissionMatcherService<DataPermission> {

    @Override
    public Collection<DataPermission> matcher(@NonNull Object context,
                                              @NonNull Collection<DataPermission> permissions) {
        if (!(context instanceof String)) {
            throw new UnsupportedOperationException("不支持的上下文");
        }

        return permissions.stream().filter(item -> !item.matcher((String) context)).collect(Collectors.toList());

    }

}
