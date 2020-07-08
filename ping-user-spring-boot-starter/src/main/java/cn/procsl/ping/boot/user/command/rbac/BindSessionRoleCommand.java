package cn.procsl.ping.boot.user.command.rbac;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;

/**
 * @author procsl
 * @date 2020/07/08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BindSessionRoleCommand implements Serializable {

    @NotNull Long sessionId;

    @NotEmpty Collection<Long> roles;

}
