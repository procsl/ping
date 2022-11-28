package cn.procsl.ping.boot.system.web.rbac;

import lombok.Data;

import java.util.Collection;

@Data
public class RolePermissionVO extends RoleVO {

    Collection<PermissionVO> permissions;

}
