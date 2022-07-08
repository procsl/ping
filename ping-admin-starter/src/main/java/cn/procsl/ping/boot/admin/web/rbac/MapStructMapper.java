package cn.procsl.ping.boot.admin.web.rbac;

import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import cn.procsl.ping.boot.admin.domain.rbac.Role;
import org.mapstruct.Mapper;


@Mapper
public interface MapStructMapper {

    RoleVO mapper(Role role);

    PermissionVO mapper(Permission permission);
}
