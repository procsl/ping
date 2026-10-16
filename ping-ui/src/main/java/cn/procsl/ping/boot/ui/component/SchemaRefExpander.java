package cn.procsl.ping.boot.ui.component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * OpenAPI {@code $ref} 展开器。
 *
 * <p>将 operation 的 request/response schema 树中所有 {@code $ref} 递归展开为目标
 * schema 的内容，使前端拿到的组件 JSON 完全内联、无需运行时解析。
 *
 * <p>解析规则（design Step 2）：
 * <ul>
 *   <li>{@code $ref} 为 JSON Pointer（如 {@code #/components/schemas/UserQuery}），
 *       相对 OpenAPI 文档根解析</li>
 *   <li>{@code $ref} 同级属性（如 parameter 级 {@code required: true}）与目标合并，
 *       同级属性优先</li>
 *   <li>嵌套 {@code $ref}（object properties 与 array items）递归展开</li>
 *   <li>展开过程检测循环引用，命中即抛 {@link CircularReferenceException}</li>
 *   <li>目标不存在时抛 {@link ReferenceResolveException}</li>
 * </ul>
 *
 * <p>输入树不会被修改，返回值为展开后的新树。
 */
public class SchemaRefExpander {

    private static final String REF_KEY = "$ref";

    private static final String POINTER_PREFIX = "#/";

    private final ObjectMapper objectMapper;
    private final JsonNode rootDocument;

    /**
     * @param objectMapper JSON 工厂
     * @param rootDocument OpenAPI 文档根节点，用于解析 {@code #/...} 指针
     */
    public SchemaRefExpander(ObjectMapper objectMapper, JsonNode rootDocument) {
        this.objectMapper = objectMapper;
        this.rootDocument = rootDocument;
    }

    /**
     * 展开给定 schema 树中的所有 {@code $ref}。
     *
     * @param schema 待展开的 schema 节点
     * @return 展开后的新节点（原节点不被修改）
     * @throws CircularReferenceException 检测到循环引用时
     * @throws ReferenceResolveException  {@code $ref} 目标不存在时
     */
    public JsonNode expand(JsonNode schema) {
        return expand(schema, new ArrayDeque<>());
    }

    private JsonNode expand(JsonNode node, Deque<String> stack) {
        if (node == null) {
            return null;
        }
        if (node.isArray()) {
            ArrayNode array = objectMapper.createArrayNode();
            for (JsonNode element : (ArrayNode) node) {
                array.add(expand(element, stack));
            }
            return array;
        }
        if (node.isObject()) {
            return expandObject((ObjectNode) node, stack);
        }
        return node;
    }

    private JsonNode expandObject(ObjectNode object, Deque<String> stack) {
        JsonNode refNode = object.get(REF_KEY);
        if (refNode != null && refNode.isString()) {
            return expandRef(refNode.asString(), object, stack);
        }

        ObjectNode result = objectMapper.createObjectNode();
        for (Map.Entry<String, JsonNode> property : object.properties()) {
            result.set(property.getKey(), expand(property.getValue(), stack));
        }
        return result;
    }

    private JsonNode expandRef(String ref, ObjectNode origin, Deque<String> stack) {
        if (stack.contains(ref)) {
            throw new CircularReferenceException(ref, List.copyOf(stack));
        }

        JsonNode target = resolve(ref);
        if (target == null) {
            throw new ReferenceResolveException(
                    "Unresolvable $ref '" + ref + "': target not found in OpenAPI document");
        }

        ObjectNode merged = merge(target.deepCopy(), origin);
        stack.addLast(ref);
        try {
            return expandObject(merged, stack);
        } finally {
            stack.removeLast();
        }
    }

    /** 合并目标 schema 与 {@code $ref} 同级属性，同级属性优先 */
    private ObjectNode merge(JsonNode target, ObjectNode origin) {
        ObjectNode merged = objectMapper.createObjectNode();
        if (target.isObject()) {
            for (Map.Entry<String, JsonNode> property : target.properties()) {
                merged.set(property.getKey(), property.getValue().deepCopy());
            }
        }
        for (Map.Entry<String, JsonNode> property : origin.properties()) {
            if (REF_KEY.equals(property.getKey())) {
                continue;
            }
            merged.set(property.getKey(), property.getValue().deepCopy());
        }
        return merged;
    }

    /** 按 JSON Pointer 相对文档根解析，非内部指针或路径不存在返回 {@code null} */
    private JsonNode resolve(String ref) {
        if (ref == null || !ref.startsWith(POINTER_PREFIX)) {
            return null;
        }
        JsonNode current = rootDocument;
        for (String token : ref.substring(POINTER_PREFIX.length()).split("/")) {
            String key = token.replace("~1", "/").replace("~0", "~");
            if (current == null || !current.isObject()) {
                return null;
            }
            current = current.get(key);
        }
        return current;
    }
}
