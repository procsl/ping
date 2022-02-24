package cn.procsl.ping.boot.user.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;

@Data
@Schema(description = "创建角色DTO")
public class CreateRoleDTO implements Serializable {

    @NotBlank @Size(max = 20) String name;

    @NotNull @Size(max = 100) Collection<@Size(max = 100) String> permissions;

}
