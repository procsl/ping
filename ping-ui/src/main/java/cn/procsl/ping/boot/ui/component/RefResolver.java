package cn.procsl.ping.boot.ui.component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code ref} → UUID 解析器。
 *
 * <p>编译阶段把 input/output 组件的路径式 {@code ref}
 * （{@code <operationId>.<request|response>.<fieldPath>}）解析为 operation 树中的
 * 具体字段节点，生成 UUID 写入该字段的 {@code id} 属性，并把组件的 {@code ref}
 * 值替换为该 UUID（design D3）。
 *
 * <p>规则：
 * <ul>
 *   <li>字段路径支持 {@code []} 表示数组 items（如 {@code content[].account}）</li>
 *   <li>同一 {@code ref} 复用相同 UUID（同一字段不重复生成）</li>
 *   <li>{@code ref} 仅限所属 operation，跨 operation 报错</li>
 *   <li>路径格式非法、operation/字段不存在时报错</li>
 * </ul>
 *
 * <p>输入的 operation 树应已完成 {@code $ref} 展开（见 {@link SchemaRefExpander}），
 * 否则字段路径无法定位。
 */
public class RefResolver {

    private static final String REQUEST = "request";

    private static final String RESPONSE = "response";

    private static final String ARRAY_ITEM_SUFFIX = "[]";

    private static final String REF_KEY = "ref";

    private static final String ID_KEY = "id";

    private static final String OPERATION_KEY = "operation";

    private static final String TYPE_KEY = "type";

    private static final String PAGINATION_TYPE = "pagination";

    private static final List<String> PAGINATION_REF_KEYS =
            List.of("offset_ref", "size_ref", "total_ref", "content_ref");

    /** operationId → operation properties（含 request/response，已 $ref 展开） */
    private final Map<String, JsonNode> operations;

    /** ref 路径 → UUID 缓存，同一字段复用 */
    private final Map<String, String> refCache = new HashMap<>();

    public RefResolver(Map<String, JsonNode> operations) {
        this.operations = Map.copyOf(operations);
    }

    /**
     * 解析 {@code ref} 路径并物化 UUID。
     *
     * @param ref             路径式 ref，如 {@code findUsers.request.query.name}
     * @param ownerOperationId 所属 operation（view/form 的 {@code properties.operation}），可为 {@code null}
     * @return 字段 UUID
     * @throws RefResolveException  路径格式非法或跨 operation 时
     * @throws RefNotFoundException operation 或字段不存在时
     */
    public String resolve(String ref, String ownerOperationId) {
        List<String> parts = validate(ref);
        String operationId = parts.get(0);
        String side = parts.get(1);

        if (ownerOperationId != null && !ownerOperationId.equals(operationId)) {
            throw new RefResolveException("Cross-operation ref '" + ref + "': component belongs to operation '"
                    + ownerOperationId + "', but ref targets '" + operationId + "'");
        }

        String cached = refCache.get(ref);
        if (cached != null) {
            return cached;
        }

        JsonNode node = locate(operationId, side, parts.subList(2, parts.size()), ref);
        String uuid = UUID.randomUUID().toString();
        ((ObjectNode) node).put(ID_KEY, uuid);
        refCache.put(ref, uuid);
        return uuid;
    }

    /** 就地替换 input/output 组件的 {@code ref} 值为 UUID；pagination 组件处理其分页引用 */
    public void resolveComponent(JsonNode component, String ownerOperationId) {
        if (PAGINATION_TYPE.equals(component.path(TYPE_KEY).asString())) {
            resolvePagination(component, ownerOperationId);
            return;
        }

        JsonNode properties = component.get("properties");
        if (properties == null || !properties.isObject()) {
            return;
        }
        JsonNode refNode = properties.get(REF_KEY);
        if (refNode == null || !refNode.isString()) {
            return;
        }
        String uuid = resolve(refNode.asString(), ownerOperationId);
        ((ObjectNode) properties).put(REF_KEY, uuid);
    }

    /** 解析 view/form 容器下所有 input/output 子节点（owner 取容器的 {@code properties.operation}） */
    public void resolveContainer(JsonNode container) {
        JsonNode properties = container.get("properties");
        JsonNode operationNode = properties == null ? null : properties.get(OPERATION_KEY);
        String owner = operationNode != null && operationNode.isString() ? operationNode.asString() : null;

        JsonNode children = container.get("children");
        if (children == null || !children.isArray()) {
            return;
        }
        for (JsonNode child : (ArrayNode) children) {
            resolveComponent(child, owner);
        }
    }

    /**
     * 解析 pagination 容器的分页引用（{@code offset_ref}/{@code size_ref}/{@code total_ref}/{@code content_ref}）
     * 及其 children 中的 output ref。
     *
     * @param pagination         pagination 组件节点
     * @param ownerOperationId   所属 operation
     */
    public void resolvePagination(JsonNode pagination, String ownerOperationId) {
        JsonNode properties = pagination.get("properties");
        if (properties != null && properties.isObject()) {
            for (String key : PAGINATION_REF_KEYS) {
                JsonNode refNode = properties.get(key);
                if (refNode == null || !refNode.isString()) {
                    continue;
                }
                String uuid = resolve(refNode.asString(), ownerOperationId);
                ((ObjectNode) properties).put(key, uuid);
            }
        }

        JsonNode children = pagination.get("children");
        if (children != null && children.isArray()) {
            for (JsonNode child : (ArrayNode) children) {
                resolveComponent(child, ownerOperationId);
            }
        }
    }

    /** 校验 ref 格式并拆分，返回 {@code [operationId, request|response, ...fieldSegments]} */
    private List<String> validate(String ref) {
        if (ref == null || ref.isBlank()) {
            throw new RefResolveException("Ref path must not be blank");
        }
        List<String> parts = Arrays.asList(ref.split("\\."));
        if (parts.size() < 3) {
            throw new RefResolveException("Invalid ref path '" + ref
                    + "': expected <operationId>.<request|response>.<fieldPath>");
        }
        String side = parts.get(1);
        if (!REQUEST.equals(side) && !RESPONSE.equals(side)) {
            throw new RefResolveException("Invalid ref path '" + ref
                    + "': second segment must be 'request' or 'response', got '" + side + "'");
        }
        return parts;
    }

    /** 沿字段路径定位到具体 schema 节点 */
    private JsonNode locate(String operationId, String side, List<String> fieldSegments, String ref) {
        JsonNode operation = operations.get(operationId);
        if (operation == null) {
            throw new RefNotFoundException(ref, "operation '" + operationId + "' not found");
        }
        JsonNode node = operation.get(side);
        if (node == null || !node.isObject()) {
            throw new RefNotFoundException(ref, "'" + side + "' not found in operation '" + operationId + "'");
        }

        for (String segment : fieldSegments) {
            boolean arrayItem = segment.endsWith(ARRAY_ITEM_SUFFIX);
            String name = arrayItem
                    ? segment.substring(0, segment.length() - ARRAY_ITEM_SUFFIX.length())
                    : segment;
            if (name.isEmpty()) {
                throw new RefNotFoundException(ref, "empty field segment '" + segment + "'");
            }
            JsonNode field = propertiesOf(node).get(name);
            if (field == null) {
                throw new RefNotFoundException(ref, "field '" + name + "' not found under '" + side
                        + "' of operation '" + operationId + "'");
            }
            node = arrayItem ? requireItems(field, ref, name) : field;
        }
        return node;
    }

    /** 取 schema 的字段容器：object schema 取 {@code properties}，字段映射（request/response）取自身 */
    private JsonNode propertiesOf(JsonNode node) {
        JsonNode properties = node.get("properties");
        return properties != null && properties.isObject() ? properties : node;
    }

    private JsonNode requireItems(JsonNode field, String ref, String name) {
        JsonNode items = field.get("items");
        if (items == null || !items.isObject()) {
            throw new RefNotFoundException(ref, "field '" + name + "' has no items schema for '[]' access");
        }
        return items;
    }
}
