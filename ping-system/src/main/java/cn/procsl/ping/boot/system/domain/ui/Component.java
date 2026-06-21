package cn.procsl.ping.boot.system.domain.ui;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 组件定义
 */
@Getter
@Setter(value = AccessLevel.PACKAGE)
public class Component implements Serializable {

    String name;
    String type;

    Map<String, Object> attributes;

    public void addAttribute(String key, Object value) {
        if (attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(key, value);
    }

}
