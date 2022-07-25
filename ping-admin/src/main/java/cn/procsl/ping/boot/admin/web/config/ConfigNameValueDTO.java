package cn.procsl.ping.boot.admin.web.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ConfigNameValueDTO implements Serializable {

    @NonNull String name;

    String content;

    public ConfigNameValueDTO(@NonNull String name, String content) {
        this.name = name;
        this.content = content;
    }
}
