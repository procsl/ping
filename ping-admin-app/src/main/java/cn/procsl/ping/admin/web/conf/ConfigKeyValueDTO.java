package cn.procsl.ping.admin.web.conf;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ConfigKeyValueDTO implements Serializable {

    @NonNull String key;

    String content;

    public ConfigKeyValueDTO(@NonNull String key, String content) {
        this.key = key;
        this.content = content;
    }
}
