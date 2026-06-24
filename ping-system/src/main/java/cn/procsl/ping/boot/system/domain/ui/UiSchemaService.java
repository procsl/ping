
package cn.procsl.ping.boot.system.domain.ui;

import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UiSchemaService {

    private final UiSchemaRepository repository = new UiSchemaRepository(new JsonMapper());

    public List<UiComponent> loadAll() {
        List<UiComponent> pages = repository.findAll();
        JsonNode doc = repository.loadOpenapiDoc();

        Map<String, JsonNode> apiMap = compileApiMap(doc);

        for (UiComponent page : pages) {
            resolveUiComponentApi(page, apiMap);
        }

        return pages;
    }

    private Map<String, JsonNode> compileApiMap(JsonNode doc) {
        Map<String, JsonNode> apiMap = new HashMap<>();
        JsonNode paths = doc.path("paths");
        JsonNode globalSchemas = doc.path("components").path("schemas");

        if (paths.isMissingNode()) {
            return apiMap;
        }

        for (var pathEntry : paths.properties()) {
            String path = pathEntry.getKey();

            for (var methodEntry : pathEntry.getValue().properties()) {
                String method = methodEntry.getKey();
                JsonNode operation = methodEntry.getValue();
                String summary = operation.path("summary").asString();

                if (summary.isBlank()) {
                    continue;
                }

                if (operation.deepCopy() instanceof ObjectNode wrapper) {
                    processSingleApiNode(wrapper, path, method, globalSchemas);
                    apiMap.put(summary, wrapper);
                }
            }
        }
        return apiMap;
    }

    private void processSingleApiNode(ObjectNode wrapper, String path, String method, JsonNode globalSchemas) {
        wrapper.put("path", path);
        wrapper.put("method", method.toUpperCase());
        wrapper.remove("tags");
        wrapper.remove("summary");

        // 1. 处理并打平 requestBody
        flattenRequestBody(wrapper);

        // 2. 处理、打平并重命名 responses -> response
        flattenAndRenameResponses(wrapper);

        // 3. 递归解析 $ref 引用
        resolveReferences(wrapper, globalSchemas, new HashSet<>());

        // 4. 递归清除整个节点中的 example
        stripExamples(wrapper);
    }

    /**
     * 打平 requestBody 结构
     */
    private void flattenRequestBody(ObjectNode wrapper) {
        if (!(wrapper.path("requestBody") instanceof ObjectNode requestBody)) {
            return;
        }

        JsonNode contentNode = requestBody.path("content");
        if (contentNode instanceof ObjectNode contentObj) {
            contentObj.properties().stream().findFirst().ifPresent(firstContent -> {
                requestBody.put("contentType", firstContent.getKey());
                JsonNode mediaTypeObj = firstContent.getValue();
                if (!mediaTypeObj.path("schema").isMissingNode()) {
                    requestBody.set("schema", mediaTypeObj.path("schema"));
                }
            });
            requestBody.remove("content");
        }
    }

    /**
     * 打平 2xx 响应结构，并将其重命名为单数 "response"
     */
    private void flattenAndRenameResponses(ObjectNode wrapper) {
        if (!(wrapper.path("responses") instanceof ObjectNode responses)) {
            return;
        }

        // 提取原有的 responses，找寻第一个 2xx 节点
        var targetEntry = responses.properties().stream()
            .filter(entry -> entry.getKey().startsWith("2"))
            .findFirst();

        // 移除旧的复数键名 "responses"
        wrapper.remove("responses");

        if (targetEntry.isEmpty()) {
            return;
        }

        String statusCode = targetEntry.get().getKey();
        if (!(targetEntry.get().getValue() instanceof ObjectNode flatResponse)) {
            return;
        }

        flatResponse.put("status", statusCode);

        // 提取 content 内部结构
        JsonNode contentNode = flatResponse.path("content");
        if (contentNode instanceof ObjectNode contentObj) {
            contentObj.properties().stream().findFirst().ifPresent(firstContent -> {
                flatResponse.put("contentType", firstContent.getKey());
                if (!firstContent.getValue().path("schema").isMissingNode()) {
                    flatResponse.set("schema", firstContent.getValue().path("schema"));
                }
            });
            flatResponse.remove("content");
        }

        // 重新绑定为单数的 "response"
        wrapper.set("response", flatResponse);
    }

    private void resolveReferences(JsonNode node, JsonNode globalSchemas, Set<String> visited) {
        if (node == null || node.isMissingNode()) {
            return;
        }

        if (node instanceof ObjectNode objectNode) {
            if (objectNode.has("$ref")) {
                String refPath = objectNode.get("$ref").asString();
                String schemaName = refPath.substring(refPath.lastIndexOf('/') + 1);

                if (!visited.add(schemaName)) {
                    objectNode.remove("$ref");
                    objectNode.put("$circularRef", schemaName);
                    return;
                }

                JsonNode targetSchema = globalSchemas.path(schemaName);
                if (!targetSchema.isMissingNode()) {
                    objectNode.remove("$ref");
                    objectNode.setAll((ObjectNode) targetSchema.deepCopy());
                    resolveReferences(objectNode, globalSchemas, new HashSet<>(visited));
                }
                return;
            }

            objectNode.properties().forEach(entry ->
                resolveReferences(entry.getValue(), globalSchemas, new HashSet<>(visited))
            );

        } else if (node.isArray()) {
            for (JsonNode element : node) {
                resolveReferences(element, globalSchemas, new HashSet<>(visited));
            }
        }
    }

    private void stripExamples(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return;
        }

        if (node instanceof ObjectNode objectNode) {
            objectNode.remove("example");
            objectNode.properties().forEach(entry -> stripExamples(entry.getValue()));
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                stripExamples(element);
            }
        }
    }

    private void resolveUiComponentApi(UiComponent component, Map<String, JsonNode> apiMap) {
        if (component == null) {
            return;
        }

        if (component.getApi() != null) {
            component.setApiDefinition(apiMap.get(component.getApi()));
        }

        if (component.getFunctions() != null) {
            for (UiComponent child : component.getFunctions()) {
                resolveUiComponentApi(child, apiMap);
            }
        }
    }
}
