package cn.procsl.ping.boot.system.domain.ac;


import lombok.Data;

import java.util.Map;

/**
 * 意图与行为特征（操作）
 */
@Data
public final class Action implements AttributeContainer {
    private final String name;               // 操作名称（如 "READ", "DELETE", "EXPORT"）
    private final Map<String, Object> attributes; // 操作的量化特征属性（如 batchSize）

    public Action(String name) {
        this(name, Map.of());
    }

    public Action(String name, Map<String, Object> attributes) {
        this.name = name != null ? name.toUpperCase() : null;
        this.attributes = attributes != null ? Map.copyOf(attributes) : Map.of();
    }

    @Override public String getType() { return name; }
}
