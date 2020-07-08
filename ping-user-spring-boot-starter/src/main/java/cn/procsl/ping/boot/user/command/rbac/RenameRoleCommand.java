package cn.procsl.ping.boot.user.command.rbac;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

import static cn.procsl.ping.boot.user.domain.rbac.entity.Role.NAME_LENGTH;

/**
 * @author procsl
 * @date 2020/07/08
 */
@Value
public class RenameRoleCommand implements Serializable {

    @NotNull Long roleId;

    @NotBlank @Size(min = 1, max = NAME_LENGTH) String name;

}
