package cn.procsl.ping.boot.system.api.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ConfigDTO extends ConfigNameValueDTO implements Serializable {

    String description;

    public ConfigDTO(@NonNull String name, String content, String description) {
        super(name, content);
        this.description = description;
    }
}
