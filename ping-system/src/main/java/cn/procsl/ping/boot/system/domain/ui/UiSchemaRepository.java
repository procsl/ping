package cn.procsl.ping.boot.system.domain.ui;

import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.MissingNode;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class UiSchemaRepository {

    private final JsonMapper mapper;

    public List<UiComponent> findAll() {

        try {

            Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:ui/**/index.json");

            List<UiComponent> result = new ArrayList<>();

            for (Resource resource : resources) {

                try (InputStream is = resource.getInputStream()) {

                    result.add(mapper.readValue(is, UiComponent.class));
                }
            }

            return result;

        } catch (Exception e) {
            throw new IllegalStateException("扫描UI定义失败", e);
        }
    }

    public JsonNode loadOpenapiDoc() {

        try (InputStream is = new ClassPathResource("ping-api-doc/openapi.json").getInputStream()) {

            return mapper.readTree(is);
        } catch (Exception e) {
            throw new IllegalStateException("加载 openapi.json 失败", e);
        }
    }

//    private JsonNode buildSchema(JsonNode operation) {
//
//        ObjectNode result = JsonNodeFactory.instance.objectNode();
//
//        /*
//         * parameters
//         */
//        JsonNode parameters = operation.path("parameters");
//
//        if (!parameters.isMissingNode() && parameters.isArray()) {
//
//            ArrayNode array = JsonNodeFactory.instance.arrayNode();
//
//            for (JsonNode parameter : parameters) {
//
//                ObjectNode item = JsonNodeFactory.instance.objectNode();
//
//                item.put("name", parameter.path("name").asString());
//
//                item.put("in", parameter.path("in").asString());
//
//                item.put("required", parameter.path("required").asBoolean());
//
//                item.put("description", parameter.path("description").asString());
//
//                item.set("schema", resolveSchema(parameter.path("schema"), new HashSet<>()));
//
//                array.add(item);
//            }
//
//            result.set("parameters", array);
//        }
//
//        /*
//         * request
//         */
//        JsonNode requestBody = operation.path("requestBody");
//
//        JsonNode requestSchema = extractSchema(requestBody);
//
//        if (!requestSchema.isMissingNode()) {
//
//            result.set("request", resolveSchema(requestSchema, new HashSet<>()));
//        }
//
//        /*
//         * response
//         */
//        JsonNode responses = operation.path("responses");
//
//        if (!responses.isMissingNode()) {
//
//            for (var entry : responses.properties()) {
//
//                String code = entry.getKey();
//
//                if (!code.startsWith("2")) {
//                    continue;
//                }
//
//                JsonNode responseSchema = extractSchema(entry.getValue());
//
//                if (!responseSchema.isMissingNode()) {
//
//                    result.set("response", resolveSchema(responseSchema, new HashSet<>()));
//
//                    break;
//                }
//            }
//        }
//
//        return result;
//    }

    private JsonNode extractSchema(JsonNode node) {

        JsonNode content = node.path("content");

        if (content.isMissingNode()) {
            return MissingNode.getInstance();
        }

        for (var entry : content.properties()) {

            JsonNode schema = entry.getValue().path("schema");

            if (!schema.isMissingNode()) {
                return schema;
            }
        }

        return MissingNode.getInstance();
    }

//    private JsonNode resolveSchema(JsonNode schema, Set<String> visited) {
//
//        if (schema == null || schema.isMissingNode()) {
//
//            return MissingNode.getInstance();
//        }
//
//        String ref = schema.path("$ref").asString();
//
//        /*
//         * ref
//         */
//        if (!ref.isBlank()) {
//
//            String schemaName = ref.substring(ref.lastIndexOf('/') + 1);
//
//            if (!visited.add(schemaName)) {
//
//                ObjectNode circular = JsonNodeFactory.instance.objectNode();
//
//                circular.put("type", "object");
//
//                circular.put("$circular", schemaName);
//
//                return circular;
//            }
//
//            return resolveSchema(schemas.path(schemaName), visited);
//        }
//
//        /*
//         * object
//         */
//        ObjectNode result = JsonNodeFactory.instance.objectNode();
//
//        schema.properties().forEach(entry -> {
//
//            String field = entry.getKey();
//
//            JsonNode value = entry.getValue();
//
//            if ("properties".equals(field)) {
//
//                ObjectNode props = JsonNodeFactory.instance.objectNode();
//
//                value.properties().forEach(p ->
//
//                    props.set(p.getKey(), resolveSchema(p.getValue(), new HashSet<>(visited))));
//
//                result.set("properties", props);
//
//                return;
//            }
//
//            if ("items".equals(field)) {
//
//                result.set("items", resolveSchema(value, new HashSet<>(visited)));
//
//                return;
//            }
//
//            result.set(field, value.deepCopy());
//        });
//
//        return result;
//    }
}
