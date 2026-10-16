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
 * {@code openapi://} 字段路径 → UUID 解析器。
 *
 * <p>编译阶段把 input/output 组件的 {@code properties.$ref}
 * （{@code openapi://<operationId>/<request|response>/<fields-glob> }，如
 * {@code openapi://findUsers/request/query/name}）解析为 operation 树中的具体字段节点，
 * 生成 UUID 写入该字段的 {@code id} 属性，并把组件的 {@code $ref} 值替换为该 UUID（design D3）。
 *
 * <p>规则：
 * <ul>
 *   <li>路径格式固定为 {@code openapi://<operationId>/<request|response>/<fieldsPath>}，
 *       字段段之间以 {@code /} 分隔</li>
 *   <li>数组元素字段经 {@code items} 段访问（如 {@code response/content/items/account}，
 *       对应 response.content 的 items.account）</li>
 *   <li>同一 {@code $ref} 复用相同 UUID（同一字段不重复生成）</li>
 *   <li>{@code $ref} 仅限所属 operation，跨 operation 报错</li>
 *   <li>缺少 {@code openapi://} 前缀、路径段缺失或非法时报错</li>
 *   <li>operation / 字段不存在时报错 {@link RefNotFoundException}（含完整 $ref 路径）</li>
 * </ul>
 *
 * <p>输入的 operation 树应已完成 {@code $ref} 展开（见 {@link SchemaRefExpander}），
 * 否则字段路径无法定位。
 */
public class RefResolver {

    /** pagination properties 中承载分页引用的键（值为 {@code {"$ref": <openapi://...>}}） */
    private static final List<String> PAGINATION_REF_KEYS = List.of("offset", "size", "total", "content");

    /** operationId → operation properties（含 request/response，已 $ref 展开） */
    private final Map<String, JsonNode> operations;

    /** $ref 路径 → UUID 缓存，同一字段复用 */
    private final Map<String, String> refCache = new HashMap<>();

    public RefResolver(Map<String, JsonNode> operations) {
        this.operations = Map.copyOf(operations);
    }

    /**
     * 解析 {@code openapi://} 字段路径并物化 UUID。
     *
     * @param ref             openapi:// 协议字段路径，如 {@code openapi://findUsers/request/query/name}
     * @param ownerOperationId 所属 operation（view/form 的 {@code properties.operation}），可为 {@code null}
     * @return 字段 UUID
     * @throws ComposeCompileException  路径格式非法或跨 operation 时
     * @throws RefNotFoundException operation 或字段不存在时
     */
    public String resolve(String ref, String ownerOperationId) {
        List<String> parts = validate(ref);
        String operationId = parts.get(0);
        String side = parts.get(1);

        if (ownerOperationId != null && !ownerOperationId.equals(operationId)) {
            throw new ComposeCompileException("Cross-operation ref '" + ref + "': component belongs to operation '"
                    + ownerOperationId + "', but ref targets '" + operationId + "'");
        }

        String cached = refCache.get(ref);
        if (cached != null) {
            return cached;
        }

        JsonNode node = locate(operationId, side, parts.subList(2, parts.size()), ref);
        String uuid = UUID.randomUUID().toString();
        ((ObjectNode) node).put(Schemas.ID, uuid);
        refCache.put(ref, uuid);
        return uuid;
    }

    /** 就地替换 input/output 组件的 {@code properties.$ref} 值为 UUID；pagination 组件处理其分页引用 */
    public void resolveComponent(JsonNode component, String ownerOperationId) {
        if (Schemas.PAGINATION.equals(component.path(Schemas.TYPE).asString())) {
            resolvePagination(component, ownerOperationId);
            return;
        }

        JsonNode properties = component.get(Schemas.PROPERTIES);
        if (properties == null || !properties.isObject()) {
            return;
        }
        JsonNode refNode = properties.get(Schemas.REF);
        if (refNode == null || !refNode.isString()) {
            return;
        }
        String uuid = resolve(refNode.asString(), ownerOperationId);
        ((ObjectNode) properties).put(Schemas.REF, uuid);
    }

    /** 解析 view/form 容器下所有 input/output 子节点（owner 取容器的 {@code properties.operation}） */
    public void resolveContainer(JsonNode container) {
        JsonNode properties = container.get(Schemas.PROPERTIES);
        JsonNode operationNode = properties == null ? null : properties.get(Schemas.OPERATION);
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
     * 解析 pagination 容器的分页引用（properties 中 {@code offset}/{@code size}/{@code total}/{@code content}
     * 各节点内的 {@code $ref}）及其 children 中的 output $ref。
     *
     * @param pagination       pagination 组件节点
     * @param ownerOperationId 所属 operation
     */
    public void resolvePagination(JsonNode pagination, String ownerOperationId) {
        JsonNode properties = pagination.get(Schemas.PROPERTIES);
        if (properties != null && properties.isObject()) {
            for (String key : PAGINATION_REF_KEYS) {
                JsonNode refContainer = properties.get(key);
                if (refContainer == null || !refContainer.isObject()) {
                    continue;
                }
                JsonNode refNode = refContainer.get(Schemas.REF);
                if (refNode == null || !refNode.isString()) {
                    continue;
                }
                String uuid = resolve(refNode.asString(), ownerOperationId);
                ((ObjectNode) refContainer).put(Schemas.REF, uuid);
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
            throw new ComposeCompileException("Ref must not be blank");
        }
        if (!ref.startsWith(Schemas.REF_PROTOCOL)) {
            throw new ComposeCompileException("Invalid ref '" + ref
                    + "': must be 'openapi://<operationId>/<request|response>/<fieldPath>' ('openapi://' prefix required)");
        }

        String path = ref.substring(Schemas.REF_PROTOCOL.length());
        if (path.isEmpty() || path.endsWith("/") || path.contains("//")) {
            throw new ComposeCompileException("Invalid ref '" + ref + "': empty path segment");
        }

        List<String> parts = Arrays.asList(path.split("/"));
        if (parts.size() < 3) {
            throw new ComposeCompileException("Invalid ref '" + ref
                    + "': expected openapi://<operationId>/<request|response>/<fieldPath>");
        }
        String side = parts.get(1);
        if (!Schemas.REQUEST.equals(side) && !Schemas.RESPONSE.equals(side)) {
            throw new ComposeCompileException("Invalid ref '" + ref
                    + "': second segment must be 'request' or 'response', got '" + side + "'");
        }
        if (parts.stream().anyMatch(String::isBlank)) {
            throw new ComposeCompileException("Invalid ref '" + ref + "': empty path segment");
        }
        return parts;
    }

    /** 沿字段路径定位到具体 schema 节点（{@code items} 段即数组 schema 的 items 子节点） */
    private JsonNode locate(String operationId, String side, List<String> fieldSegments, String ref) {
        JsonNode operation = operations.get(operationId);
        if (operation == null) {
            throw new RefNotFoundException(ref, "operation '" + operationId + "' not found");
        }
        JsonNode node = operation.get(side);
        if (node == null || !node.isObject()) {
            throw new RefNotFoundException(ref, "'" + side + "' not found in operation '" + operationId + "'");
        }

        for (String name : fieldSegments) {
            JsonNode field = Schemas.propertiesOf(node).get(name);
            if (field == null) {
                throw new RefNotFoundException(ref, "field '" + name + "' not found under '" + side
                        + "' of operation '" + operationId + "'");
            }
            node = field;
        }
        return node;
    }
}
