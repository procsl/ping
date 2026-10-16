package cn.procsl.ping.boot.ui.component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * 分页自动检测 + 物化器。
 *
 * <p>编译阶段检测 operation 是否分页（design D9）：
 * <ul>
 *   <li>request 含 {@code offset} + {@code size}</li>
 *   <li>response 含 {@code content} + {@code total}</li>
 * </ul>
 *
 * <p>命中后为引用该 operation 的 view 物化：
 * <ol>
 *   <li>生成 {@code type: "input"} 组件（{@code page.offset} / {@code page.size}），
 *       插入 view children（与 query、sort 并列）</li>
 *   <li>生成 {@code type: "pagination"} 容器，children 为 view 的 output 子节点，
 *       插入 view children 末尾</li>
 *   <li>pagination 的 {@code offset_ref}/{@code size_ref}/{@code total_ref}/{@code content_ref}
 *       写入字段路径（由 {@link RefResolver} 在编译后段解析为 UUID）</li>
 * </ol>
 *
 * <p>{@code mode} 取 view 的 {@code properties.pagination.mode}，默认 {@code page}，
 * 支持 {@code scroll}。
 */
public class PaginationMaterializer {

    private static final String TYPE = "type";

    private static final String ID = "id";

    private static final String PROPERTIES = "properties";

    private static final String CHILDREN = "children";

    private static final String REF = "ref";

    private static final String INPUT = "input";

    private static final String OUTPUT = "output";

    private static final String PAGINATION = "pagination";

    private static final String OPERATION = "operation";

    private static final String MODE = "mode";

    private static final String DEFAULT_MODE = "page";

    private static final String OFFSET = "offset";

    private static final String SIZE = "size";

    private static final String CONTENT = "content";

    private static final String TOTAL = "total";

    private static final String OFFSET_REF = "offset_ref";

    private static final String SIZE_REF = "size_ref";

    private static final String CONTENT_REF = "content_ref";

    private static final String TOTAL_REF = "total_ref";

    private static final String PAGE_PREFIX = "page.";

    private final ObjectMapper objectMapper;

    public PaginationMaterializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 分页检测：request 有 offset + size 且 response 有 content + total。
     *
     * @param operationProperties operation 的 properties 节点
     * @return 是否分页
     */
    public boolean isPaged(JsonNode operationProperties) {
        JsonNode request = operationProperties.get("request");
        JsonNode response = operationProperties.get("response");
        if (request == null || !request.isObject() || response == null || !response.isObject()) {
            return false;
        }
        return request.has(OFFSET) && request.has(SIZE)
                && response.has(CONTENT) && response.has(TOTAL);
    }

    /**
     * 为 view 物化 offset/size input 与 pagination 容器。非分页 operation 时为 no-op。
     *
     * @param view                view 组件节点
     * @param operationProperties 该 view 引用 operation 的 properties 节点
     */
    public void materialize(JsonNode view, JsonNode operationProperties) {
        if (!isPaged(operationProperties)) {
            return;
        }

        JsonNode viewProperties = view.get(PROPERTIES);
        String operationId = viewProperties == null ? null : viewProperties.path(OPERATION).asString();
        if (operationId == null || operationId.isEmpty()) {
            throw new IllegalArgumentException(
                    "View '" + view.path(ID).asString() + "' has no 'properties.operation'");
        }

        // 1. 收集 output 子节点，其余保留
        ArrayNode outputs = objectMapper.createArrayNode();
        ArrayNode remaining = objectMapper.createArrayNode();
        JsonNode children = view.get(CHILDREN);
        if (children != null && children.isArray()) {
            for (JsonNode child : (ArrayNode) children) {
                if (OUTPUT.equals(child.path(TYPE).asString())) {
                    outputs.add(child);
                } else {
                    remaining.add(child);
                }
            }
        }

        // 2. 生成 offset / size input
        ObjectNode offsetInput = input(operationId, OFFSET);
        ObjectNode sizeInput = input(operationId, SIZE);

        // 3. 生成 pagination 容器（children 为 output 子节点）
        ObjectNode pagination = objectMapper.createObjectNode();
        pagination.put(TYPE, PAGINATION);
        pagination.put(ID, PAGE_PREFIX + view.path(ID).asString());

        ObjectNode paginationProperties = objectMapper.createObjectNode();
        paginationProperties.put(MODE, resolveMode(viewProperties));
        paginationProperties.put(OFFSET_REF, operationId + ".request." + OFFSET);
        paginationProperties.put(SIZE_REF, operationId + ".request." + SIZE);
        paginationProperties.put(TOTAL_REF, operationId + ".response." + TOTAL);
        paginationProperties.put(CONTENT_REF, operationId + ".response." + CONTENT);
        pagination.set(PROPERTIES, paginationProperties);
        pagination.set(CHILDREN, outputs);

        // 4. 重写 view children：非 output + offset/size input + pagination（末尾）
        ArrayNode newChildren = objectMapper.createArrayNode();
        remaining.forEach(newChildren::add);
        newChildren.add(offsetInput);
        newChildren.add(sizeInput);
        newChildren.add(pagination);
        ((ObjectNode) view).set(CHILDREN, newChildren);
    }

    private ObjectNode input(String operationId, String name) {
        ObjectNode input = objectMapper.createObjectNode();
        input.put(TYPE, INPUT);
        input.put(ID, PAGE_PREFIX + name);
        input.set(PROPERTIES, objectMapper.createObjectNode().put(REF, operationId + ".request." + name));
        return input;
    }

    /** 取 view 的 {@code properties.pagination.mode}，默认 {@code page} */
    private String resolveMode(JsonNode viewProperties) {
        if (viewProperties == null) {
            return DEFAULT_MODE;
        }
        JsonNode pagination = viewProperties.get(PAGINATION);
        if (pagination != null && pagination.isObject()) {
            JsonNode mode = pagination.get(MODE);
            if (mode != null && mode.isString()) {
                return mode.asString();
            }
        }
        return DEFAULT_MODE;
    }
}
