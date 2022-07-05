package cn.procsl.ping.admin.web.rbac;

import cn.procsl.ping.boot.base.domain.rbac.Permission;
import cn.procsl.ping.boot.base.domain.rbac.Role;
import org.mapstruct.Mapper;


@Mapper
public interface MapStructMapper {

    RoleVO mapper(Role role);

    PermissionVO mapper(Permission permission);
}
