package cn.procsl.ping.boot.system.domain.ac;

import lombok.Data;

import java.util.Map;

@Data
public class Environment {

    private final Map<String, Object> attributes;

    public Environment(Map<String, Object> attributes) {
        this.attributes = attributes != null ? Map.copyOf(attributes) : Map.of();
    }

}
