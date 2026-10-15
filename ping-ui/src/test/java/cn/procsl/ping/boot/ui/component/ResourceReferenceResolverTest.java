package cn.procsl.ping.boot.ui.component;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.*;

class ResourceReferenceResolverTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 编译输入 fixture：取自 design.md 的真实 openapi.json 片段，
     * 并补充 multipart body、无成功响应、required 参数三个场景。
     */
    private JsonNode openApi() {
        return objectMapper.readTree("""
                {
                  "paths": {
                    "/v1/system/users": {
                      "get": {
                        "tags": ["User"],
                        "summary": "获取用户列表",
                        "operationId": "findUsers",
                        "parameters": [
                          {
                            "name": "query",
                            "in": "query",
                            "required": true,
                            "schema": { "$ref": "#/components/schemas/UserQuery" }
                          },
                          {
                            "name": "offset",
                            "in": "query",
                            "description": "基于从1开始的分页 (1..N)",
                            "schema": { "type": "integer", "format": "int32", "default": 1 }
                          },
                          {
                            "name": "size",
                            "in": "query",
                            "schema": { "type": "integer", "format": "int32", "default": 10 }
                          },
                          {
                            "name": "sort",
                            "in": "query",
                            "schema": { "type": "array", "items": { "type": "string" } }
                          }
                        ],
                        "responses": {
                          "200": {
                            "description": "OK",
                            "content": {
                              "application/json": {
                                "schema": { "$ref": "#/components/schemas/FormatPageUserRecord" }
                              }
                            }
                          }
                        }
                      }
                    },
                    "/v1/system/users/{id}/account": {
                      "patch": {
                        "tags": ["User"],
                        "summary": "修改用户账户状态",
                        "operationId": "accountStatusSetting",
                        "parameters": [
                          {
                            "name": "id",
                            "in": "path",
                            "required": true,
                            "schema": { "type": "integer", "format": "int64" }
                          }
                        ],
                        "requestBody": {
                          "content": {
                            "application/json": {
                              "schema": { "$ref": "#/components/schemas/AccountStateDTO" }
                            }
                          },
                          "required": true
                        },
                        "responses": {
                          "204": { "description": "No Content" }
                        }
                      }
                    },
                    "/v1/system/users/{id}/avatar": {
                      "post": {
                        "operationId": "uploadAvatar",
                        "parameters": [
                          { "name": "id", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } }
                        ],
                        "requestBody": {
                          "content": {
                            "multipart/form-data": {
                              "schema": { "type": "object", "properties": { "file": { "type": "string", "format": "binary" } } }
                            }
                          }
                        },
                        "responses": {
                          "204": { "description": "No Content" }
                        }
                      }
                    },
                    "/v1/system/required-flag": {
                      "get": {
                        "operationId": "requiredFlag",
                        "parameters": [
                          { "name": "state", "in": "query", "required": true, "schema": { "type": "string" } }
                        ],
                        "responses": {
                          "200": {
                            "content": { "application/json": { "schema": { "type": "object" } } }
                          }
                        }
                      }
                    },
                    "/v1/system/broken": {
                      "get": {
                        "operationId": "brokenStatus",
                        "responses": {
                          "400": { "description": "Bad Request" }
                        }
                      }
                    }
                  }
                }
                """);
    }

    private ResourceReferenceResolver resolver() {
        return new ResourceReferenceResolver(objectMapper, openApi());
    }

    // --- 1.2.1 按 operationId 匹配 path + method ---

    @Test
    void shouldMatchOperationByOperationId() {
        ObjectNode properties = resolver().resolve("findUsers");
        assertEquals("GET", properties.get("method").asText());
        assertEquals("/v1/system/users", properties.get("path").asText());

        ObjectNode patch = resolver().resolve("accountStatusSetting");
        assertEquals("PATCH", patch.get("method").asText());
        assertEquals("/v1/system/users/{id}/account", patch.get("path").asText());
    }

    // --- 1.2.2 未找到 operationId 时报错（含可用列表） ---

    @Test
    void shouldThrowReferenceNotFoundExceptionForUnknownOperationId() {
        ReferenceNotFoundException e = assertThrows(ReferenceNotFoundException.class,
                () -> resolver().resolve("notExists"));

        assertEquals("notExists", e.getOperationId());
        assertTrue(e.getAvailableOperationIds().contains("findUsers"));
        assertTrue(e.getMessage().contains("findUsers"), "异常信息应包含可用 operationId 列表");
    }

    // --- 1.2.3 重复 operationId 检测 ---

    @Test
    void shouldRejectDuplicateOperationId() {
        JsonNode document = objectMapper.readTree("""
                {
                  "paths": {
                    "/v1/a": { "get": { "operationId": "dup" } },
                    "/v1/b": { "post": { "operationId": "dup" } }
                  }
                }
                """);

        ReferenceResolveException e = assertThrows(ReferenceResolveException.class,
                () -> new ResourceReferenceResolver(objectMapper, document));
        assertTrue(e.getMessage().contains("dup"), "异常信息应包含重复的 operationId");
    }

    // --- 1.2.4 parameters 解析：query/path 两种参数位置 ---

    @Test
    void shouldParseQueryAndPathParameters() {
        ObjectNode findUsers = resolver().resolve("findUsers");
        ObjectNode request = (ObjectNode) findUsers.get("request");

        // $ref 对象封装的 query 参数 → request.query
        assertEquals("#/components/schemas/UserQuery", request.get("query").get("$ref").asText());
        // 标量 query 参数 → request.<name>
        assertEquals("integer", request.get("offset").get("type").asText());
        assertEquals(1, request.get("offset").get("default").asInt());
        assertEquals("integer", request.get("size").get("type").asText());
        assertEquals("array", request.get("sort").get("type").asText());

        // path 参数 → request.path.<name>
        ObjectNode account = resolver().resolve("accountStatusSetting");
        JsonNode pathParams = account.get("request").get("path");
        assertNotNull(pathParams, "path 参数应放入 request.path 容器");
        assertEquals("integer", pathParams.get("id").get("type").asText());
    }

    // --- 1.2.5 requestBody 解析 → request.body ---

    @Test
    void shouldParseRequestBodyAsRequestDotBody() {
        ObjectNode properties = resolver().resolve("accountStatusSetting");
        JsonNode body = properties.get("request").get("body");

        assertNotNull(body, "requestBody 应解析为 request.body");
        assertEquals("#/components/schemas/AccountStateDTO", body.get("$ref").asText());
    }

    // --- 1.2.6 requestBody 非 application/json 时跳过 ---

    @Test
    void shouldSkipNonJsonRequestBody() {
        ObjectNode properties = resolver().resolve("uploadAvatar");

        assertNull(properties.get("request").get("body"),
                "multipart/form-data 的 requestBody 应被跳过，不生成 request.body");
    }

    // --- 1.2.7 responses 解析：200 取 schema / 204 为空对象 ---

    @Test
    void shouldParseSuccessAndNoContentResponses() {
        // 200 → content.application/json.schema
        ObjectNode findUsers = resolver().resolve("findUsers");
        assertEquals("#/components/schemas/FormatPageUserRecord",
                findUsers.get("response").get("$ref").asText());

        // 204 → 空对象
        ObjectNode account = resolver().resolve("accountStatusSetting");
        ObjectNode response = (ObjectNode) account.get("response");
        assertTrue(response.isEmpty(), "204 响应应解析为空对象");
    }

    // --- 1.2.8 无成功响应时报错 ---

    @Test
    void shouldThrowWhenNoSuccessResponse() {
        ReferenceResolveException e = assertThrows(ReferenceResolveException.class,
                () -> resolver().resolve("brokenStatus"));
        assertTrue(e.getMessage().contains("brokenStatus"), "异常信息应包含 operationId");
    }

    // --- 1.2.9 parameter 级 required 标记移入 schema ---

    @Test
    void shouldMarkRequiredOnParameterSchema() {
        ObjectNode properties = resolver().resolve("requiredFlag");
        JsonNode state = properties.get("request").get("state");

        assertTrue(state.get("required").asBoolean(false),
                "parameter.required=true 应在 schema 上标记 required: true");

        // 对照：未标注 required 的参数不生成该标记
        ObjectNode findUsers = resolver().resolve("findUsers");
        assertFalse(findUsers.get("request").get("offset").has("required"));
    }
}
