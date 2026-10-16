package cn.procsl.ping.boot.ui.component;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.*;

class OperationRefResolverTest {

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

    private OperationRefResolver resolver() {
        return new OperationRefResolver(objectMapper, openApi());
    }

    // --- 5.1.1 openapi:// 前缀解析 ---

    @Test
    void shouldExtractOperationIdFromOpenApiProtocolRef() {
        assertEquals("findUsers", resolver().extractOperationId("openapi://findUsers"));
        assertEquals("accountStatusSetting", resolver().extractOperationId("openapi://accountStatusSetting"));
    }

    @Test
    void shouldRejectRefWithoutOpenApiProtocolPrefix() {
        OperationRefResolver resolver = resolver();

        ComposeCompileException noPrefix = assertThrows(ComposeCompileException.class,
                () -> resolver.resolve("findUsers"), "旧格式的裸 operationId 不再被接受");
        assertTrue(noPrefix.getMessage().contains("openapi://"));

        ComposeCompileException otherProtocol = assertThrows(ComposeCompileException.class,
                () -> resolver.resolve("http://findUsers"));
        assertTrue(otherProtocol.getMessage().contains("openapi://"));
    }

    @Test
    void shouldRejectInvalidOpenApiProtocolRef() {
        OperationRefResolver resolver = resolver();

        // 空白
        assertThrows(ComposeCompileException.class, () -> resolver.resolve("  "));
        // 有前缀但 operationId 为空
        assertThrows(ComposeCompileException.class, () -> resolver.resolve("openapi://"));
        assertThrows(ComposeCompileException.class, () -> resolver.resolve("openapi://  "));
        // operation 级引用不允许携带字段路径（字段路径由 RefResolver 处理）
        assertThrows(ComposeCompileException.class,
                () -> resolver.resolve("openapi://findUsers/request/query/name"));
    }

    // --- 5.1.3 operationId 匹配：openapi://<operationId> 格式 ---

    @Test
    void shouldMatchOperationByOpenApiProtocolRef() {
        ObjectNode properties = resolver().resolve("openapi://findUsers");
        assertEquals("GET", properties.get("method").asText());
        assertEquals("/v1/system/users", properties.get("path").asText());

        ObjectNode patch = resolver().resolve("openapi://accountStatusSetting");
        assertEquals("PATCH", patch.get("method").asText());
        assertEquals("/v1/system/users/{id}/account", patch.get("path").asText());
    }

    @Test
    void shouldThrowRefNotFoundExceptionForUnknownOperationId() {
        RefNotFoundException e = assertThrows(RefNotFoundException.class,
                () -> resolver().resolve("openapi://notExists"));

        assertEquals("openapi://notExists", e.getRef());
        assertTrue(e.getAvailableOperationIds().contains("findUsers"), "operation 级断链应携带可用 operationId 列表");
        assertTrue(e.getMessage().contains("notExists"), "异常信息应包含被引用的 operationId");
        assertTrue(e.getMessage().contains("findUsers"), "异常信息应包含可用 operationId 列表");
    }

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

        ComposeCompileException e = assertThrows(ComposeCompileException.class,
                () -> new OperationRefResolver(objectMapper, document));
        assertTrue(e.getMessage().contains("dup"), "异常信息应包含重复的 operationId");
    }

    // --- 1.2.4 parameters 解析：query/path 两种参数位置 ---

    @Test
    void shouldParseQueryAndPathParameters() {
        ObjectNode findUsers = resolver().resolve("openapi://findUsers");
        ObjectNode request = (ObjectNode) findUsers.get("request");

        // $ref 对象封装的 query 参数 → request.query
        assertEquals("#/components/schemas/UserQuery", request.get("query").get("$ref").asText());
        // 标量 query 参数 → request.<name>
        assertEquals("integer", request.get("offset").get("type").asText());
        assertEquals(1, request.get("offset").get("default").asInt());
        assertEquals("integer", request.get("size").get("type").asText());
        assertEquals("array", request.get("sort").get("type").asText());

        // path 参数 → request.path.<name>
        ObjectNode account = resolver().resolve("openapi://accountStatusSetting");
        JsonNode pathParams = account.get("request").get("path");
        assertNotNull(pathParams, "path 参数应放入 request.path 容器");
        assertEquals("integer", pathParams.get("id").get("type").asText());
    }

    // --- 1.2.5 requestBody 解析 → request.body ---

    @Test
    void shouldParseRequestBodyAsRequestDotBody() {
        ObjectNode properties = resolver().resolve("openapi://accountStatusSetting");
        JsonNode body = properties.get("request").get("body");

        assertNotNull(body, "requestBody 应解析为 request.body");
        assertEquals("#/components/schemas/AccountStateDTO", body.get("$ref").asText());
    }

    // --- 1.2.6 requestBody 非 application/json 时跳过 ---

    @Test
    void shouldSkipNonJsonRequestBody() {
        ObjectNode properties = resolver().resolve("openapi://uploadAvatar");

        assertNull(properties.get("request").get("body"),
                "multipart/form-data 的 requestBody 应被跳过，不生成 request.body");
    }

    // --- 1.2.7 responses 解析：200 取 schema / 204 为空对象 ---

    @Test
    void shouldParseSuccessAndNoContentResponses() {
        // 200 → content.application/json.schema
        ObjectNode findUsers = resolver().resolve("openapi://findUsers");
        assertEquals("#/components/schemas/FormatPageUserRecord",
                findUsers.get("response").get("$ref").asText());

        // 204 → 空对象
        ObjectNode account = resolver().resolve("openapi://accountStatusSetting");
        ObjectNode response = (ObjectNode) account.get("response");
        assertTrue(response.isEmpty(), "204 响应应解析为空对象");
    }

    // --- 1.2.8 无成功响应时报错 ---

    @Test
    void shouldThrowWhenNoSuccessResponse() {
        ComposeCompileException e = assertThrows(ComposeCompileException.class,
                () -> resolver().resolve("openapi://brokenStatus"));
        assertTrue(e.getMessage().contains("brokenStatus"), "异常信息应包含 operationId");
    }

    // --- 1.2.9 parameter 级 required 标记移入 schema ---

    @Test
    void shouldMarkRequiredOnParameterSchema() {
        ObjectNode properties = resolver().resolve("openapi://requiredFlag");
        JsonNode state = properties.get("request").get("state");

        assertTrue(state.get("required").asBoolean(false),
                "parameter.required=true 应在 schema 上标记 required: true");

        // 对照：未标注 required 的参数不生成该标记
        ObjectNode findUsers = resolver().resolve("openapi://findUsers");
        assertFalse(findUsers.get("request").get("offset").has("required"));
    }

    // --- 5.3.2 operation 的 api 字段校验（openapi:// 协议） ---

    @Test
    void shouldResolveApiRefToContract() {
        JsonNode component = objectMapper.readTree("""
                {
                  "type": "operation",
                  "id": "findUsers",
                  "properties": {
                    "api": { "$ref": "openapi://findUsers" }
                  }
                }
                """);

        ObjectNode contract = resolver().resolveApi(component);
        assertEquals("GET", contract.get("method").asText());
        assertEquals("/v1/system/users", contract.get("path").asText());
        assertNotNull(contract.get("request"));
        assertNotNull(contract.get("response"));
    }

    @Test
    void shouldRejectOperationWithoutApiAnnotation() {
        OperationRefResolver resolver = resolver();

        // api 未标注
        ComposeCompileException missing = assertThrows(ComposeCompileException.class,
                () -> resolver.resolveApi(objectMapper.readTree("""
                        { "type": "operation", "id": "no-api", "properties": {} }
                        """)));
        assertTrue(missing.getMessage().contains("no-api"), "异常信息应包含 operation id");

        // properties 缺失
        assertThrows(ComposeCompileException.class,
                () -> resolver.resolveApi(objectMapper.readTree("{ \"type\": \"operation\", \"id\": \"no-props\" }")));
    }

    @Test
    void shouldRejectOperationWithInvalidApiStructure() {
        OperationRefResolver resolver = resolver();

        // api 不是对象
        ComposeCompileException notObject = assertThrows(ComposeCompileException.class,
                () -> resolver.resolveApi(objectMapper.readTree("""
                        { "type": "operation", "id": "bad-api", "properties": { "api": "openapi://findUsers" } }
                        """)));
        assertTrue(notObject.getMessage().contains("bad-api"));

        // api 缺少 $ref
        assertThrows(ComposeCompileException.class,
                () -> resolver.resolveApi(objectMapper.readTree("""
                        { "type": "operation", "id": "no-ref", "properties": { "api": {} } }
                        """)));
    }

    @Test
    void shouldRejectOperationWithWrongApiProtocolFormat() {
        OperationRefResolver resolver = resolver();

        // 旧格式：裸 operationId（非 openapi://）
        ComposeCompileException bare = assertThrows(ComposeCompileException.class,
                () -> resolver.resolveApi(objectMapper.readTree("""
                        { "type": "operation", "id": "legacy", "properties": { "api": { "$ref": "findUsers" } } }
                        """)));
        assertTrue(bare.getMessage().contains("openapi://"));

        // 字段路径也属于非法的 operation 级 api 引用
        assertThrows(ComposeCompileException.class,
                () -> resolver.resolveApi(objectMapper.readTree("""
                        { "type": "operation", "id": "deep",
                          "properties": { "api": { "$ref": "openapi://findUsers/request/query/name" } } }
                        """)));
    }

    @Test
    void shouldRejectOperationWithUnresolvableApiRef() {
        RefNotFoundException e = assertThrows(RefNotFoundException.class,
                () -> resolver().resolveApi(objectMapper.readTree("""
                        { "type": "operation", "id": "broken",
                          "properties": { "api": { "$ref": "openapi://notExists" } } }
                        """)));
        assertEquals("openapi://notExists", e.getRef());
    }
}
