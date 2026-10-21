package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.exception.ComposeException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 编译上下文：持有全部 openapi 完整文档（一个或多个，将来分模块导出即多文档），
 * 并在构造期从中构建 operationId 索引（design D4/D7）。
 *
 * <p>索引条目 {@link Operation} 携带 operation 定义镜像（原 operation 深拷贝 + 注入
 * {@code method}/{@code path}）与<b>来源文档</b>——{@code $ref} 展开按来源文档的
 * {@code components} 解析（文档隔离，不同文档的同名 schema 互不影响）。
 *
 * <p>构造期规则：
 * <ul>
 *   <li>缺 {@code operationId} 的 operation 不进索引（不强制，D4——被引用找不到才报断链）</li>
 *   <li>跨文档 operationId 冲突（不同定义）抛 {@link ComposeException}（启动失败，不兜底）</li>
 *   <li>同一定义重复出现（重复扫描）容忍</li>
 * </ul>
 *
 * <p>无状态：直接 new 创建、用完即释放（AGENTS DDD 无状态原则）。
 */
public class CompileContext {

    /**
     * operationId 对应的索引条目：定义镜像 + 来源文档。
     *
     * @param definition     operation 定义镜像（含注入的 method/path）
     * @param sourceDocument 该 operation 所属的完整 openapi 文档（$ref 展开的 components 来源）
     */
    public record Operation(ObjectNode definition, JsonNode sourceDocument) {

        public String method() {
            return definition.get("method").asString();
        }

        public String path() {
            return definition.get("path").asString();
        }

        /**
         * 来源文档的 components.schemas；文档无 components 时 null（调用方按断链处理）。
         */
        public JsonNode schemas() {
            JsonNode components = sourceDocument.get("components");
            return components != null && components.isObject() ? components.get("schemas") : null;
        }
    }

    private static final Set<String> HTTP_METHODS =
        Set.of("get", "put", "post", "delete", "options", "head", "patch", "trace");

    private final List<JsonNode> documents;
    private final Map<String, Operation> operations;

    public CompileContext(List<JsonNode> documents) {
        this.documents = List.copyOf(documents);
        this.operations = buildIndex(this.documents);
    }

    /**
     * @return 全部 openapi 完整文档（不可变列表）
     */
    public List<JsonNode> documents() {
        return documents;
    }

    /**
     * 按 operationId 查索引条目。
     *
     * @return 条目；不存在时 null（调用方据此抛断链错误并附声明来源）
     */
    public Operation operation(String operationId) {
        return operations.get(operationId);
    }

    public boolean has(String operationId) {
        return operations.containsKey(operationId);
    }

    /**
     * @return 不可变 operationId 索引
     */
    public Map<String, Operation> operations() {
        return operations;
    }

    private static Map<String, Operation> buildIndex(List<JsonNode> documents) {
        Map<String, Operation> index = new LinkedHashMap<>();
        for (JsonNode document : documents) {
            JsonNode paths = document.get("paths");
            if (paths == null || paths.isNull()) {
                continue;
            }
            if (!paths.isObject()) {
                throw new ComposeException(
                    "'paths' must be a JSON object, got: " + paths.getNodeType());
            }
            paths.properties().forEach(pathEntry ->
                indexPathItem(index, pathEntry.getKey(), pathEntry.getValue(), document));
        }
        return Map.copyOf(index);
    }

    private static void indexPathItem(Map<String, Operation> index, String path,
                                      JsonNode pathItem, JsonNode document) {
        if (!pathItem.isObject()) {
            throw new ComposeException(
                "path item '" + path + "' must be a JSON object, got: " + pathItem.getNodeType());
        }

        pathItem.properties().forEach(methodEntry -> {
            String method = methodEntry.getKey();
            if (!HTTP_METHODS.contains(method.toLowerCase())) {
                return; // 路径级 parameters/summary/$ref 等非 operation 键：忽略
            }

            JsonNode operation = methodEntry.getValue();
            if (!operation.isObject()) {
                throw new ComposeException("operation " + method.toUpperCase() + " " + path
                    + " must be a JSON object, got: " + operation.getNodeType());
            }

            JsonNode operationId = operation.get("operationId");
            if (operationId == null || !operationId.isTextual() || operationId.asString().isEmpty()) {
                return; // 缺 operationId 不强制（D4）
            }

            ObjectNode mirror = ((ObjectNode) operation).deepCopy();
            mirror.put("method", method.toUpperCase());
            mirror.put("path", path);
            putOrConflict(index, operationId.asString(), mirror, document);
        });
    }

    private static void putOrConflict(Map<String, Operation> index, String operationId,
                                      ObjectNode mirror, JsonNode document) {
        Operation existing = index.get(operationId);
        if (existing == null) {
            index.put(operationId, new Operation(mirror, document));
            return;
        }
        if (existing.definition().equals(mirror)) {
            return; // 同一定义重复出现：容忍
        }
        throw new ComposeException(
            "Conflicting operationId '" + operationId + "' in OpenAPI document: "
                + existing.method() + " " + existing.path()
                + " and " + mirror.get("method").asString() + " " + mirror.get("path").asString());
    }
}
