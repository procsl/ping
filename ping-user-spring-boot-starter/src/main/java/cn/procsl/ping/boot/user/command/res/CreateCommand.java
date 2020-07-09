package cn.procsl.ping.boot.user.command.res;

import cn.procsl.ping.boot.user.domain.res.entity.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;

/**
 * @author procsl
 * @date 2020/07/09
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateCommand implements Serializable {

    @NotBlank @Size(max = 20, min = 1) String name;

    @NotNull ResourceType type;

    Long parentId;

    Set<Long> depends;

}
