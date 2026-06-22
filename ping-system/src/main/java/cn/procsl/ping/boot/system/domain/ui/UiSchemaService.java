package cn.procsl.ping.boot.system.domain.ui;


import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class UiSchemaService {

    private final JsonMapper jsonMapper = JsonMapper.shared();
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    /**
     * 加载并拼装所有菜单
     */
    public JsonNode loadAndAssembleAllMenus() throws IOException {
        // 1. 加载全局 OpenAPI 的 API 映射表，同时获取整个原始的 components/schemas 节点
        JsonNode openApiRoot = loadOpenApiRoot();
        Map<String, JsonNode> openApiMap = parseOpenApiMap(openApiRoot);
        JsonNode globalSchemas = openApiRoot != null && openApiRoot.get("components") != null
            ? openApiRoot.get("components").get("schemas") : null;

        // 2. 检索并遍历所有菜单文件
        Resource[] menuResources = resolver.getResources("classpath:ui/*/index.json");
        ArrayNode allMenusResult = jsonMapper.createArrayNode();

        for (Resource resource : menuResources) {
            if (!resource.exists()) continue;

            try (InputStream is = resource.getInputStream()) {
                JsonNode menuRoot = jsonMapper.readTree(is);
                if (!(menuRoot instanceof ObjectNode menuObj)) continue;

                // 3. 处理当前菜单引用，并收集当前菜单用到的所有 $ref
                Set<String> referencedSchemaNames = new HashSet<>();
                processMenuFunctions(menuObj, openApiMap, referencedSchemaNames);

                // 4. 根据收集到的初始 ref，递归追溯所有深层嵌套的关联 ref
                ObjectNode menuSchemasNode = jsonMapper.createObjectNode();
                populateMenuSchemas(referencedSchemaNames, globalSchemas, menuSchemasNode);

                // 5. 在菜单根层级注入 schemas 字段
                menuObj.set("schemas", menuSchemasNode);
                allMenusResult.add(menuObj);
            }
        }
        return allMenusResult;
    }

    /**
     * 遍历处理 functions，并收集第一层 API 结构里出现的所有 $ref
     */
    private void processMenuFunctions(ObjectNode menuObj, Map<String, JsonNode> openApiMap, Set<String> refContainer) {
        JsonNode functions = menuObj.get("functions");
        if (functions == null || !functions.isArray()) return;

        for (JsonNode functionNode : functions) {
            if (!(functionNode instanceof ObjectNode funcObj)) continue;

            JsonNode refNode = funcObj.get("reference");
            if (refNode == null || !refNode.isString()) continue;

            String referenceText = refNode.asString();
            if (!referenceText.startsWith("@API:")) continue;

            String summaryKey = referenceText.substring(5).trim();
            JsonNode matchedApi = openApiMap.get(summaryKey);
            if (matchedApi == null) continue;

            funcObj.set("reference", matchedApi);

            // 扫描这个 API 内部所有的 $ref 节点
            findRefKeys(matchedApi, refContainer);
        }
    }

    /**
     * 深度优先/递归提取 Json 节点树中所有包含 "$ref" 的值（提取出最后的类名）
     */
    private void findRefKeys(JsonNode node, Set<String> container) {
        if (node == null) return;

        if (node.has("$ref")) {
            String refValue = node.get("$ref").asString();
            // 例如从 "#/components/schemas/UserVO" 截取出 "UserVO"
            String schemaName = refValue.substring(refValue.lastIndexOf("/") + 1);
            container.add(schemaName);
        }

        // 无论是对象还是数组，直接利用 Jackson 3 的原生特性向下隐式迭代
        for (JsonNode child : node) {
            findRefKeys(child, container);
        }
    }

    /**
     * 递归追溯：有些 Schema 内部还引用了其他 Schema，必须连坐式全部连根拔起
     */
    private void populateMenuSchemas(Set<String> refNames, JsonNode globalSchemas, ObjectNode resultNode) {
        if (globalSchemas == null || refNames.isEmpty()) return;

        // 复制一份当前需要处理的 key，防止在循环中直接修改集合导致并发修改异常
        Set<String> currentRound = new HashSet<>(refNames);
        refNames.clear(); // 清空以备下一轮存储新发现的深层依赖

        for (String schemaName : currentRound) {
            if (resultNode.has(schemaName)) continue;

            JsonNode schemaDefinition = globalSchemas.get(schemaName);
            if (schemaDefinition == null) continue;

            // 存入当前菜单的 schemas 结果集中
            resultNode.set(schemaName, schemaDefinition);

            // 检查这个 Schema 内部是否又潜伏了其他的 $ref 依赖
            Set<String> deepRefs = new HashSet<>();
            findRefKeys(schemaDefinition, deepRefs);

            // 排除已经存在于结果集中的，剩下的放进下一轮备查
            for (String deepRef : deepRefs) {
                if (resultNode.has(deepRef)) continue;
                refNames.add(deepRef);
            }
        }

        // 递归进入下一层，直到没有新发现的 $ref 为止
        populateMenuSchemas(refNames, globalSchemas, resultNode);
    }

    /**
     * 获取 OpenAPI 的根节点
     */
    private JsonNode loadOpenApiRoot() throws IOException {
        Resource openApiResource = resolver.getResource("classpath:ping-api-doc/openapi.json");
        if (!openApiResource.exists()) return null;
        try (InputStream is = openApiResource.getInputStream()) {
            return jsonMapper.readTree(is);
        }
    }

    /**
     * 解析生成 summary -> API 定义的映射表
     */
    private Map<String, JsonNode> parseOpenApiMap(JsonNode openApiRoot) {
        Map<String, JsonNode> apiMap = new HashMap<>();
        if (openApiRoot == null) return apiMap;

        JsonNode paths = openApiRoot.get("paths");
        if (paths == null) return apiMap;

        for (String path : paths.propertyNames()) {
            extractApiMethods(path, paths.get(path), apiMap);
        }
        return apiMap;
    }

    /**
     * 抽取单个路径下的方法
     */
    private void extractApiMethods(String path, JsonNode methods, Map<String, JsonNode> apiMap) {
        if (methods == null) return;

        for (String propertyName : methods.propertyNames()) {
            JsonNode apiInfo = methods.get(propertyName);
            if (apiInfo == null) continue;

            JsonNode summaryNode = apiInfo.get("summary");
            if (summaryNode == null) continue;

            ObjectNode details = jsonMapper.createObjectNode();
            details.put("path", path);
            details.put("method", propertyName);
            details.set("summary", summaryNode);

            if (apiInfo.get("operationId") != null) details.set("operationId", apiInfo.get("operationId"));
            if (apiInfo.get("parameters") != null) details.set("parameters", apiInfo.get("parameters"));
            if (apiInfo.get("requestBody") != null) details.set("requestBody", apiInfo.get("requestBody"));
            if (apiInfo.get("responses") != null) details.set("responses", apiInfo.get("responses"));

            apiMap.put(summaryNode.asText().trim(), details);
        }
    }
}
