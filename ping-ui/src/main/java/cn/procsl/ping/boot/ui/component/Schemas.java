package cn.procsl.ping.boot.ui.component;

import tools.jackson.databind.JsonNode;

/**
 * schema 树共享常量与导航工具。
 *
 * <p>组件编译各阶段（operation 解析、$ref 展开、UUID 物化、分页物化、默认值合并）
 * 共同操作的 JSON 键名与 {@code openapi://} 协议前缀统一定义于此，
 * 各组件不再重复声明，避免多份相同字面量漂移。
 *
 * <p>仅限包内使用，不对外发布。
 */
final class Schemas {

    /** openapi:// 协议前缀 */
    static final String REF_PROTOCOL = "openapi://";

    /** JSON Schema 引用键 */
    static final String REF = "$ref";

    /** 组件 / 字段类型键 */
    static final String TYPE = "type";

    /** 组件标识键（声明侧）与字段 UUID 键（schema 侧） */
    static final String ID = "id";

    /** 组件属性容器键 */
    static final String PROPERTIES = "properties";

    /** 组件子节点键 */
    static final String CHILDREN = "children";

    /** view/form 关联的 operationId 键 */
    static final String OPERATION = "operation";

    /** operation 契约的输入侧 */
    static final String REQUEST = "request";

    /** operation 契约的输出侧 */
    static final String RESPONSE = "response";

    /** 数组 schema 的元素 schema 键 */
    static final String ITEMS = "items";

    /** 分页容器组件类型 */
    static final String PAGINATION = "pagination";

    private Schemas() {
    }

    /**
     * 取 schema 的字段容器：object schema 取 {@code properties}，
     * 字段映射（request/response 直接承载字段）取自身。
     */
    static JsonNode propertiesOf(JsonNode node) {
        JsonNode properties = node.get(PROPERTIES);
        return properties != null && properties.isObject() ? properties : node;
    }
}
