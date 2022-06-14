package cn.procsl.ping.admin.web.rbac;

import cn.procsl.ping.boot.infra.domain.rbac.HttpPermission;
import cn.procsl.ping.boot.infra.domain.rbac.PagePermission;
import cn.procsl.ping.boot.infra.domain.rbac.Permission;
import com.querydsl.core.types.dsl.EntityPathBase;

import java.util.function.BiFunction;

public enum PermissionType {


    http(HttpPermission::create, QHttpPermission.httpPermission), page(PagePermission::new, QPagePermission.pagePermission);

    final BiFunction<String, String, Permission> factory;
    final EntityPathBase<? extends Permission> query;

    PermissionType(BiFunction<String, String, Permission> factory, EntityPathBase<? extends Permission> type) {
        this.factory = factory;
        query = type;
    }

}
