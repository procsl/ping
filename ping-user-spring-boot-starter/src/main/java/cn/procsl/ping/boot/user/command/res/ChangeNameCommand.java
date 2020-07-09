package cn.procsl.ping.boot.user.command.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author procsl
 * @date 2020/07/09
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangeNameCommand implements Serializable {

    @NotNull Long resourceId;

    @NotBlank @Size(max = 20, min = 1) String name;
}
