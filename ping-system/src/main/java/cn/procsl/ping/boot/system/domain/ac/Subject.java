package cn.procsl.ping.boot.system.domain.ac;

import lombok.Data;

import java.util.Map;

/**
 * 访问发起者（主体）
 */
@Data
public final class Subject implements AttributeContainer {
    private final String id;                 // 主体唯一标识（如用户ID "user_1001"）
    private final String type;               // 主体类型（如 "ADMIN", "MEMBER", "APP"）
    private final Map<String, Object> attributes; // 主体的属性池

    public Subject(String id, String type, Map<String, Object> attributes) {
        this.id = id;
        this.type = type != null ? type.toUpperCase() : null;
        this.attributes = attributes != null ? Map.copyOf(attributes) : Map.of();
    }

}
