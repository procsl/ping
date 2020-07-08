package cn.procsl.ping.boot.user.command.rbac;

import cn.procsl.ping.boot.user.domain.rbac.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;

import static cn.procsl.ping.boot.user.domain.rbac.entity.Role.NAME_LENGTH;

/**
 * @author procsl
 * @date 2020/07/08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoleCommand implements Serializable {

    @NotBlank @Size(min = 1, max = NAME_LENGTH) String name;

    Long inheritRoleId;

    Collection<Permission> permissions;

}
