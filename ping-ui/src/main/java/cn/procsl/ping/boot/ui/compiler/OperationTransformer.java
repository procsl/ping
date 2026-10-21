package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.domain.Component;
import cn.procsl.ping.boot.ui.exception.ComposeException;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * operation 内联阶段（本次唯一核心 Transformer，design D2/D3）：
 * 按 {@code properties.operationId} 定位 operation，把该 operation 的
 * method/path/parameters/requestBody/responses 内联进 {@code function.properties}，
 * 展开所有 {@code $ref}（来源文档 components，文档隔离），
 * responses 只保留 2xx——产物 function 完全自包含、无 {@code $ref} 残留。
 *
 * <p>失败即启动失败（design D8）：断链、缺 operationId、循环引用抛
 * {@link cn.procsl.ping.boot.ui.exception.ComposeException}，消息含引用路径与声明来源。
 */
@Slf4j
public class OperationTransformer implements Transformer {

    /**
     * 内联进 function.properties 的 operation 键（D2 列举：method/path/parameters/requestBody/responses），
     * 用 List 保证产物键序稳定。
     */
    private static final List<String> INLINE_KEYS =
        List.of("method", "path", "parameters", "requestBody");

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Component> process(List<Component> nodes, CompileContext context) {
        return processAll(nodes, context, List.of());
    }

    private List<Component> processAll(List<Component> nodes, CompileContext context,
                                       List<String> ancestors) {
        List<Component> result = new ArrayList<>(nodes.size());
        for (Component node : nodes) {
            List<String> path = extend(ancestors, node);
            List<Component> children = processAll(node.getChildren(), context, path);
            if (node.isType("function")) {
                result.add(new Component(node.getType(), node.getId(),
                    inline(node, context, path), children));
            } else {
                // page/app：原样保留声明，仅递归子节点
                result.add(new Component(node.getType(), node.getId(), node.getProperties(), children));
            }
        }
        return List.copyOf(result);
    }

    /**
     * 内联单个 function：声明的 operationId → 索引定位 → 白名单键组装 → $ref 全展开。
     */
    private JsonNode inline(Component function, CompileContext context, List<String> path) {
        String location = String.join(" ", path);

        JsonNode declared = function.getProperties();
        JsonNode operationIdNode = declared != null ? declared.get("operationId") : null;
        if (operationIdNode == null || !operationIdNode.isString()
            || operationIdNode.asString().isEmpty()) {
            throw new ComposeException(
                "Missing 'properties.operationId' in " + location
                    + " (function must declare { \"properties\": { \"operationId\": \"...\" } })");
        }
        String operationId = operationIdNode.asString();

        // 未知属性（operationId 之外的源声明）：忽略 + debug（design D8，未来使用意图扩展位）
        declared.properties().forEach(entry -> {
            if (!"operationId".equals(entry.getKey())) {
                log.debug("忽略 function '{}' 的未知属性 '{}'（function.properties 只声明 operationId）",
                    function.getId(), entry.getKey());
            }
        });

        CompileContext.Operation operation = context.operation(operationId);
        if (operation == null) {
            throw new ComposeException("operationId '" + operationId
                + "' not found in OpenAPI document, referenced by " + location);
        }

        ObjectNode inlined = mapper.createObjectNode();
        inlined.put("operationId", operationId);
        for (String key : INLINE_KEYS) {
            JsonNode value = operation.definition().get(key);
            if (value != null) {
                inlined.set(key, value);
            }
        }
        inlined.set("responses", filter2xx(operation.definition().get("responses")));

        // 全量 $ref 展开（按来源文档 components 解析，文档隔离）
        return expand(inlined, operation.sourceDocument(), new ArrayDeque<>());
    }

    /**
     * responses 只保留 2xx 状态码（design Invariant 6），非 2xx 不序列化。
     */
    private JsonNode filter2xx(JsonNode responses) {
        ObjectNode filtered = mapper.createObjectNode();
        if (responses == null || !responses.isObject()) {
            return filtered;
        }
        responses.properties().forEach(entry -> {
            String code = entry.getKey();
            if (code.length() == 3 && code.chars().allMatch(Character::isDigit)) {
                int status = Integer.parseInt(code);
                if (status >= 200 && status < 300) {
                    filtered.set(code, entry.getValue());
                }
            }
        });
        return filtered;
    }

    // --- $ref 展开 ---

    private JsonNode expand(JsonNode node, JsonNode root, Deque<String> refStack) {
        if (node.isObject()) {
            JsonNode ref = node.get("$ref");
            if (ref != null && ref.isString()) {
                return resolveRef(ref.asString(), root, refStack);
            }
            ObjectNode copy = mapper.createObjectNode();
            node.properties().forEach(entry ->
                copy.set(entry.getKey(), expand(entry.getValue(), root, refStack)));
            return copy;
        }
        if (node.isArray()) {
            ArrayNode copy = mapper.createArrayNode();
            for (JsonNode item : node) {
                copy.add(expand(item, root, refStack));
            }
            return copy;
        }
        return node;
    }

    private JsonNode resolveRef(String ref, JsonNode root, Deque<String> refStack) {
        if (!ref.startsWith("#/")) {
            throw new ComposeException("Unsupported $ref '" + ref
                + "': only local references (#/...) are supported");
        }
        if (refStack.contains(ref)) {
            throw new ComposeException("Circular $ref detected: '" + ref
                + "' (resolution path: " + new ArrayList<>(refStack) + ")");
        }

        JsonNode target = navigate(root, ref.substring(2));
        if (target == null) {
            throw new ComposeException("Broken $ref '" + ref
                + "' not found in source OpenAPI document");
        }

        refStack.push(ref);
        try {
            return expand(target, root, refStack);
        } finally {
            refStack.pop();
        }
    }

    /**
     * JSON Pointer 本地导航（{@code components/schemas/UserRecord}，支持 ~1/~0 转义）。
     */
    private JsonNode navigate(JsonNode root, String pointer) {
        JsonNode current = root;
        for (String rawSegment : pointer.split("/")) {
            if (current == null || !current.isObject()) {
                return null;
            }
            String segment = rawSegment.replace("~1", "/").replace("~0", "~");
            current = current.get(segment);
        }
        return current;
    }

    /**
     * 祖先链：用于报错的引用路径（如 {@code page 'user' function 'list'}）。
     */
    private List<String> extend(List<String> ancestors, Component node) {
        List<String> path = new ArrayList<>(ancestors.size() + 1);
        path.addAll(ancestors);
        String id = node.getId() != null ? node.getId() : "<no-id>";
        path.add(node.getType() + " '" + id + "'");
        return path;
    }
}
