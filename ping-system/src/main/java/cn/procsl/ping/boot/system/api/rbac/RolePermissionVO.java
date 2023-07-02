package cn.procsl.ping.boot.system.api.rbac;

import lombok.Data;

import java.util.Collection;

@Data
public class RolePermissionVO extends RoleVO {

    Collection<PermissionVO> permissions;

}
