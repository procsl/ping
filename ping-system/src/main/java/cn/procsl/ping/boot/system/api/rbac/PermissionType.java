package cn.procsl.ping.boot.system.api.rbac;

import cn.procsl.ping.boot.system.domain.rbac.*;

import java.util.function.BiFunction;

public enum PermissionType {


    http(HttpPermission::create), page(PagePermission::new);

    final BiFunction<String, String, Permission> factory;

    PermissionType(BiFunction<String, String, Permission> factory) {
        this.factory = factory;
    }

}
