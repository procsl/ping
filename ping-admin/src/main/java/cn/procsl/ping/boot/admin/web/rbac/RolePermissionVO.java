package cn.procsl.ping.boot.admin.web.rbac;

import lombok.Data;

import java.util.Collection;

@Data
public class RolePermissionVO extends RoleVO {

    Collection<PermissionVO> permissions;

}
