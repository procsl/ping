package cn.procsl.ping.boot.system.domain.ac;

import lombok.Data;

import java.util.Map;

/**
 * 访问资源描述
 */

/**
 * 遭受动作的目标（资源）
 */
@Data
public final class Resource implements AttributeContainer {
    private final String type;                     // 资产来源大类 (如: "api", "button", "order")
    private final String identity;                 // 资产的业务逻辑标识 (如: "/api/orders/*", "export_btn", "ORD_99")
    Map<String, Object> attributes;

    public Resource(String type, String identity, Map<String, Object> attributes) {
        this.type = type != null ? type.toLowerCase() : "";
        this.identity = identity;
        this.attributes = attributes != null ? Map.copyOf(attributes) : Map.of();
    }
}
