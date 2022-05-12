package cn.procsl.ping.boot.system;

import cn.procsl.ping.boot.domain.valid.UniqueField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
@Validated
public class ConfigDTO implements Serializable {

    @NotNull
    @UniqueField(entity = Config.class, fieldName = "key", message = "配置项[{field}]已存在")
    String key;

    @Size(max = 100) String content;

    @Size(max = 500) String description;

}
