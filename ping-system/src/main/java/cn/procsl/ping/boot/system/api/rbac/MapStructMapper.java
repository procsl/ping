package cn.procsl.ping.boot.system.api.rbac;

import cn.procsl.ping.boot.system.domain.rbac.Permission;
import cn.procsl.ping.boot.system.domain.rbac.Role;
import org.mapstruct.Mapper;


@Mapper
public interface MapStructMapper {

    RoleVO mapper(Role role);

    RolePermissionVO mapperDetails(Role role);

    PermissionVO mapper(Permission permission);


}
