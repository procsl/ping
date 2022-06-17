package cn.procsl.ping.admin.web.rbac;

import cn.procsl.ping.boot.base.domain.rbac.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoleVO extends RoleNameDTO {

    Long id;

    public RoleVO(Role role) {
        this.setName(role.getName());
        this.setId(role.getId());
    }
}
