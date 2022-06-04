package cn.procsl.ping.admin.web.conf;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ConfigDTO extends ConfigKeyValueDTO implements Serializable {

    String description;

    public ConfigDTO(@NonNull String key, String content, String description) {
        super(key, content);
        this.description = description;
    }
}
