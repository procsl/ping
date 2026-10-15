package cn.procsl.ping.boot.ui.component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * {@code @ResourceReference} 解析器。
 *
 * <p>以打包期导出的 OpenAPI 文档为输入，按 {@code operationId} 匹配 path + method，
 * 将 {@code parameters}、{@code requestBody}、{@code responses} 解析为 operation 契约结构
 * （method/path/request/response），写入 compose.json 的 operation 节点。
 *
 * <p>解析规则（design D5/D6）：
 * <ul>
 *   <li>{@code in: path} 参数 → {@code request.path.<name>}</li>
 *   <li>{@code in: query} 且 schema 含 {@code $ref}（对象封装）→ {@code request.query}</li>
 *   <li>{@code in: query} 标量/数组参数 → {@code request.<name>}（如 offset/size/sort）</li>
 *   <li>{@code requestBody} 仅取 {@code application/json} 的 schema → {@code request.body}，
 *       其余内容类型（如 multipart/form-data）跳过</li>
 *   <li>{@code responses} 取 200/201 的 {@code content.application/json.schema} → {@code response}；
 *       204 则为空对象；无任何成功响应报错</li>
 *   <li>parameter 级 {@code required=true} 标记移入对应 schema 节点</li>
 * </ul>
 *
 * <p>schema 节点均为深拷贝，后续编译阶段（$ref 展开、ref→UUID、默认值合并）
 * 对节点的修改不会污染 OpenAPI 原文档。
 */
public class ResourceReferenceResolver {

    private static final Set<String> HTTP_METHODS =
            Set.of("get", "put", "post", "delete", "options", "head", "patch", "trace");

    private static final String JSON_CONTENT_TYPE = "application/json";

    private static final List<String> SUCCESS_WITH_BODY_STATUSES = List.of("200", "201");

    private static final String NO_CONTENT_STATUS = "204";

    private final ObjectMapper objectMapper;

    /** operationId → OpenAPI operation 索引（构建期完成重复检测） */
    private final Map<String, IndexedOperation> operations = new LinkedHashMap<>();

    /**
     * @param objectMapper      JSON 工厂
     * @param openApiDocument   打包期导出的 OpenAPI 文档根节点
     * @throws ReferenceResolveException 文档缺少 paths 或存在重复 operationId 时
     */
    public ResourceReferenceResolver(ObjectMapper objectMapper, JsonNode openApiDocument) {
        this.objectMapper = objectMapper;
        buildIndex(openApiDocument);
    }

    /**
     * 按 operationId 解析为 operation 契约结构。
     *
     * @param operationId {@code @ResourceReference} 引用的 operationId
     * @return 含 method/path/request/response 的 properties 节点
     * @throws ReferenceNotFoundException operationId 不存在时（含可用 operationId 列表）
     * @throws ReferenceResolveException  operation 无任何成功响应（200/201/204）时
     */
    public ObjectNode resolve(String operationId) {
        IndexedOperation indexed = operations.get(operationId);
        if (indexed == null) {
            throw new ReferenceNotFoundException(operationId, List.copyOf(operations.keySet()));
        }

        ObjectNode properties = objectMapper.createObjectNode();
        properties.put("method", indexed.httpMethod().toUpperCase(Locale.ROOT));
        properties.put("path", indexed.path());

        ObjectNode request = objectMapper.createObjectNode();
        parseParameters(operationId, indexed.operation(), request);
        parseRequestBody(operationId, indexed.operation(), request);

        ObjectNode response = parseResponses(operationId, indexed.operation());

        properties.set("request", request);
        properties.set("response", response);
        return properties;
    }

    /** 构建 operationId 索引，重复 operationId 立即报错（fail fast） */
    private void buildIndex(JsonNode openApiDocument) {
        JsonNode paths = openApiDocument == null ? null : openApiDocument.get("paths");
        if (paths == null || !paths.isObject()) {
            throw new ReferenceResolveException("Invalid OpenAPI document: missing 'paths' object");
        }

        for (Map.Entry<String, JsonNode> pathEntry : paths.properties()) {
            String path = pathEntry.getKey();
            JsonNode pathItem = pathEntry.getValue();
            if (pathItem == null || !pathItem.isObject()) {
                continue;
            }
            for (Map.Entry<String, JsonNode> methodEntry : pathItem.properties()) {
                String httpMethod = methodEntry.getKey();
                if (!HTTP_METHODS.contains(httpMethod)) {
                    continue;
                }
                JsonNode operation = methodEntry.getValue();
                if (operation == null || !operation.isObject()) {
                    continue;
                }
                JsonNode idNode = operation.get("operationId");
                if (idNode == null || !idNode.isString() || idNode.asString().isBlank()) {
                    continue;
                }
                String operationId = idNode.asString();
                IndexedOperation previous = operations.put(operationId, new IndexedOperation(path, httpMethod, operation));
                if (previous != null) {
                    throw new ReferenceResolveException("Duplicate operationId '" + operationId + "' in OpenAPI document: "
                            + previous.httpMethod().toUpperCase(Locale.ROOT) + " " + previous.path()
                            + " and " + httpMethod.toUpperCase(Locale.ROOT) + " " + path);
                }
            }
        }
    }

    /** 解析 parameters：按 in 分派到 request.path / request.query / request.<name> */
    private void parseParameters(String operationId, JsonNode operation, ObjectNode request) {
        JsonNode parameters = operation.get("parameters");
        if (parameters == null || !parameters.isArray()) {
            return;
        }

        for (JsonNode parameter : parameters) {
            if (parameter == null || !parameter.isObject()) {
                continue;
            }
            String in = parameter.path("in").asString(null);
            String name = parameter.path("name").asString(null);
            JsonNode schema = parameter.get("schema");
            if (in == null || name == null || schema == null || !schema.isObject()) {
                continue;
            }

            JsonNode schemaCopy = schema.deepCopy();
            if (parameter.path("required").asBoolean(false)) {
                ((ObjectNode) schemaCopy).put("required", true);
            }

            switch (in) {
                case "path" -> requestContainer(request, "path").set(name, schemaCopy);
                case "query" -> {
                    // $ref 对象封装（一个参数承载整个查询对象）→ request.query；标量/数组 → request.<name>
                    if (schemaCopy.has("$ref")) {
                        request.set("query", schemaCopy);
                    } else {
                        request.set(name, schemaCopy);
                    }
                }
                // header/cookie 参数不属于接口契约的输入输出结构
                default -> { }
            }
        }
    }

    /** 解析 requestBody：仅 application/json 的 schema → request.body，其余内容类型跳过 */
    private void parseRequestBody(String operationId, JsonNode operation, ObjectNode request) {
        JsonNode requestBody = operation.get("requestBody");
        if (requestBody == null || !requestBody.isObject()) {
            return;
        }
        JsonNode schema = extractJsonSchema(requestBody.get("content"));
        if (schema != null) {
            request.set("body", schema.deepCopy());
        }
    }

    /** 解析 responses：200/201 取 schema，204 为空对象，无成功响应报错 */
    private ObjectNode parseResponses(String operationId, JsonNode operation) {
        JsonNode responses = operation.get("responses");
        if (responses == null || !responses.isObject()) {
            throw new ReferenceResolveException("operation '" + operationId + "' has no responses definition");
        }

        for (String status : SUCCESS_WITH_BODY_STATUSES) {
            JsonNode response = responses.get(status);
            if (response == null || !response.isObject()) {
                continue;
            }
            JsonNode schema = extractJsonSchema(response.get("content"));
            // 200/201 存在但无 application/json schema → 成功但无返回字段
            return schema != null ? (ObjectNode) schema.deepCopy() : objectMapper.createObjectNode();
        }

        if (responses.has(NO_CONTENT_STATUS)) {
            return objectMapper.createObjectNode();
        }

        throw new ReferenceResolveException("operation '" + operationId + "' has no success response (200/201/204), found: "
                + responses.properties().stream().map(Map.Entry::getKey).toList());
    }

    /** 从 content 节点提取 application/json 的 schema，非 JSON 内容类型返回 null */
    private JsonNode extractJsonSchema(JsonNode content) {
        if (content == null || !content.isObject()) {
            return null;
        }
        JsonNode json = content.get(JSON_CONTENT_TYPE);
        if (json == null || !json.isObject()) {
            return null;
        }
        return json.get("schema");
    }

    /** 取（或创建）request 下的容器节点，如 request.path */
    private ObjectNode requestContainer(ObjectNode request, String name) {
        JsonNode existing = request.get(name);
        if (existing instanceof ObjectNode node) {
            return node;
        }
        ObjectNode created = objectMapper.createObjectNode();
        request.set(name, created);
        return created;
    }

    private record IndexedOperation(String path, String httpMethod, JsonNode operation) {
    }
}
