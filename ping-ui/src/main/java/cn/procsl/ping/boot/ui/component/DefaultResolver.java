package cn.procsl.ping.boot.ui.component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认值合并器。
 *
 * <p>编译阶段把 {@code operation.properties.defaults} 与容器级
 * （function/view/form）{@code properties.overrides} 合并到 operation 字段的
 * {@code default} 属性（design Step 5）。
 *
 * <p>声明结构为 request/response 的「投影」：
 * <pre>
 * "defaults":   { "request": { "query": { "state": "enable" } } }
 * "overrides":  { "request": { "state": "disable" } }
 * </pre>
 *
 * <p>字段路径两种写法均支持：
 * <ul>
 *   <li>路径式（spec）：{@code query.state}、{@code content[].account}、{@code offset}</li>
 *   <li>字段名简写（design 示例）：{@code state} —— 路径解析失败后按字段名唯一匹配</li>
 * </ul>
 *
 * <p>优先级链：<b>实际调用值（运行时） &gt; overrides（编译时） &gt; operation defaults（编译时）</b>。
 * 编译阶段先应用 operation defaults，再应用 overrides，后者覆盖前者。
 */
public class DefaultResolver {

    private static final String DEFAULTS_KEY = "defaults";

    private static final String OVERRIDES_KEY = "overrides";

    private static final String DEFAULT_KEY = "default";

    private static final String TYPE_KEY = "type";

    private static final String PROPERTIES_KEY = "properties";

    private static final String ITEMS_KEY = "items";

    private static final String ARRAY_ITEM_SUFFIX = "[]";

    private static final List<String> SIDES = List.of("request", "response");

    /**
     * 应用 operation 级 {@code defaults} 到 operation properties 树。
     *
     * @param operationProperties operation 的 properties 节点（含 method/path/request/response/defaults）
     */
    public void applyOperationDefaults(JsonNode operationProperties) {
        apply(operationProperties, operationProperties.get(DEFAULTS_KEY), false);
    }

    /**
     * 应用容器级（function/view/form）{@code overrides}，覆盖 operation defaults。
     *
     * @param containerProperties 容器（function/view/form）的 properties 节点（含 overrides）
     * @param operationProperties 目标 operation 的 properties 节点
     */
    public void applyOverrides(JsonNode containerProperties, JsonNode operationProperties) {
        apply(operationProperties, containerProperties.get(OVERRIDES_KEY), true);
    }

    private void apply(JsonNode operationProperties, JsonNode source, boolean typeCheck) {
        if (source == null || !source.isObject()) {
            return;
        }
        for (String side : SIDES) {
            JsonNode sideSource = source.get(side);
            if (sideSource == null || !sideSource.isObject()) {
                continue;
            }
            JsonNode sideSchema = operationProperties.get(side);
            if (sideSchema == null || !sideSchema.isObject()) {
                throw new DefaultResolveException(
                        "Default target side '" + side + "' not found in operation properties");
            }

            List<DefaultValue> values = new ArrayList<>();
            collect(sideSource, "", values);
            for (DefaultValue value : values) {
                JsonNode field = locate(sideSchema, value.path(), side);
                if (typeCheck) {
                    validateType(field, value.value(), value.path());
                }
                ((ObjectNode) field).set(DEFAULT_KEY, value.value().deepCopy());
            }
        }
    }

    /** 把 request/response 投影递归展平为「点分路径 → 值」列表 */
    private void collect(JsonNode node, String prefix, List<DefaultValue> result) {
        if (node == null || !node.isObject()) {
            if (prefix != null && !prefix.isEmpty()) {
                result.add(new DefaultValue(prefix, node));
            }
            return;
        }
        for (Map.Entry<String, JsonNode> property : node.properties()) {
            String key = property.getKey();
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            collect(property.getValue(), path, result);
        }
    }

    /** 定位字段：先按路径，失败后按字段名唯一匹配 */
    private JsonNode locate(JsonNode sideSchema, String path, String side) {
        JsonNode byPath = locateByPath(sideSchema, path);
        if (byPath != null) {
            return byPath;
        }
        JsonNode byName = locateByName(sideSchema, path);
        if (byName != null) {
            return byName;
        }
        throw new DefaultResolveException("Default target field '" + path + "' not found under '" + side + "'");
    }

    /** 按点分路径定位，支持 {@code []} 表示数组 items */
    private JsonNode locateByPath(JsonNode sideSchema, String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        JsonNode node = sideSchema;
        for (String segment : path.split("\\.")) {
            boolean arrayItem = segment.endsWith(ARRAY_ITEM_SUFFIX);
            String name = arrayItem
                    ? segment.substring(0, segment.length() - ARRAY_ITEM_SUFFIX.length())
                    : segment;
            if (name.isEmpty()) {
                return null;
            }
            JsonNode field = propertiesOf(node).get(name);
            if (field == null) {
                return null;
            }
            node = arrayItem ? field.get(ITEMS_KEY) : field;
            if (node == null) {
                return null;
            }
        }
        return node;
    }

    /** 按字段名递归查找唯一匹配，多个匹配视为歧义返回 {@code null} */
    private JsonNode locateByName(JsonNode node, String name) {
        JsonNode properties = node.get(PROPERTIES_KEY);
        if (properties != null && properties.isObject() && properties.has(name)) {
            return properties.get(name);
        }

        JsonNode found = null;
        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> property : node.properties()) {
                JsonNode child = property.getValue();
                if (!child.isObject()) {
                    continue;
                }
                JsonNode candidate = locateByName(child, name);
                if (candidate != null) {
                    if (found != null) {
                        return null;
                    }
                    found = candidate;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : (ArrayNode) node) {
                if (!child.isObject()) {
                    continue;
                }
                JsonNode candidate = locateByName(child, name);
                if (candidate != null) {
                    if (found != null) {
                        return null;
                    }
                    found = candidate;
                }
            }
        }
        return found;
    }

    /** 取 schema 的字段容器：object schema 取 {@code properties}，字段映射（request/response）取自身 */
    private JsonNode propertiesOf(JsonNode node) {
        JsonNode properties = node.get(PROPERTIES_KEY);
        return properties != null && properties.isObject() ? properties : node;
    }

    /** 覆盖值类型必须与字段 schema 声明的 {@code type} 一致（未声明类型则跳过） */
    private void validateType(JsonNode field, JsonNode value, String path) {
        JsonNode typeNode = field.get(TYPE_KEY);
        if (typeNode == null || !typeNode.isString()) {
            return;
        }
        String type = typeNode.asString();
        if (matchesType(type, value)) {
            return;
        }
        throw new DefaultResolveException("Override value for field '" + path + "' is " + jsonType(value)
                + " but schema declares '" + type + "'");
    }

    private boolean matchesType(String type, JsonNode value) {
        return switch (type) {
            case "string" -> value.isString();
            case "integer" -> value.isIntegralNumber();
            case "number" -> value.isNumber();
            case "boolean" -> value.isBoolean();
            case "array" -> value.isArray();
            case "object" -> value.isObject();
            case "null" -> value.isNull();
            default -> true;
        };
    }

    private String jsonType(JsonNode value) {
        if (value.isString()) {
            return "string";
        }
        if (value.isBoolean()) {
            return "boolean";
        }
        if (value.isArray()) {
            return "array";
        }
        if (value.isObject()) {
            return "object";
        }
        if (value.isNull()) {
            return "null";
        }
        if (value.isIntegralNumber()) {
            return "integer";
        }
        if (value.isNumber()) {
            return "number";
        }
        return value.getNodeType().toString();
    }

    private record DefaultValue(String path, JsonNode value) {
    }
}
