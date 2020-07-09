package cn.procsl.ping.boot.user.command.res;

import cn.procsl.ping.boot.user.domain.res.entity.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author procsl
 * @date 2020/07/09
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangeTypeCommand implements Serializable {
    @NotNull Long resourceId;

    @NotNull ResourceType type;
}
