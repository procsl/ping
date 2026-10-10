package cn.procsl.ping.boot.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.stream.Stream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 组件类型定义验证：从 classpath 固定路径 META-INF/ping/ui/types/ 读取，
 * 断言 spec（openspec/specs/ui-component-schema/spec.md）列出的 16 个常用类型全部存在且结构完整。
 *
 * <p>刻意使用固定路径逐个读取而非通配扫描，与架构要求「运行时只读固定路径」保持一致。
 */
class ComponentTypeDefinitionTest {

    /** spec「内置常用后台管理抽象组件类型」列出的 16 个类型 */
    static final List<String> SPEC_TYPES = List.of(
        "application", "layout", "menu", "nav_group", "tabs", "breadcrumb",
        "dataset", "query", "column", "row_action",
        "form", "field", "form_action",
        "action", "placeholder", "empty"
    );

    /** design D5 的 2 个框架壳体类型 */
    static final List<String> SHELL_TYPES = List.of("user_info_panel", "main_container");

    private final JsonMapper mapper = JsonMapper.builder().build();

    private JsonNode readType(String type) {
        ClassPathResource resource =
            new ClassPathResource("META-INF/ping/ui/types/" + type + ".json");
        assertTrue(resource.exists(), "缺少组件类型定义：" + type);
        try (InputStream is = resource.getInputStream()) {
            return mapper.readTree(is);
        } catch (Exception e) {
            throw new IllegalStateException("组件类型定义无法解析：" + type, e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "application", "layout", "menu", "nav_group", "tabs", "breadcrumb",
        "dataset", "query", "column", "row_action",
        "form", "field", "form_action",
        "action", "placeholder", "empty"
    })
    void specTypeExists(String type) {
        JsonNode node = readType(type);

        assertEquals(type, node.path("type").asString(),
            "类型定义的 type 必须与文件名一致");
        assertTrue(node.path("name").asString().trim().length() > 0,
            type + " 缺少展示名 name");
        assertTrue(node.path("description").asString().trim().length() > 0,
            type + " 缺少语义说明 description");
        assertTrue(node.path("children").isArray(),
            type + " 缺少允许的子节点类型 children");

        JsonNode properties = node.path("properties");
        assertTrue(properties.isArray(), type + " 的 properties 必须是数组");
        for (JsonNode property : properties.properties().stream().map(e -> e.getValue()).toList()) {
            assertTrue(property.path("name").asString().trim().length() > 0,
                type + " 存在缺少 name 的属性定义");
        }
    }

    @Test
    void shellTypesExist() {
        for (String type : SHELL_TYPES) {
            JsonNode node = readType(type);
            assertEquals(type, node.path("type").asString());
            assertTrue(node.path("description").asString().trim().length() > 0,
                type + " 缺少语义说明 description");
        }
    }

    @Test
    void specTypeCountIsSixteen() {
        assertEquals(16, SPEC_TYPES.size(), "spec 声明的常用类型应为 16 个");
        assertNotNull(SPEC_TYPES);
    }

    /**
     * 全部 18 个类型定义都必须可读且 JSON 合法。
     */
    @Test
    void allTypeDefinitionsAreValidJson() {
        for (String type : Stream.concat(SPEC_TYPES.stream(), SHELL_TYPES.stream()).toList()) {
            JsonNode node = readType(type);
            assertTrue(node.isObject(), type + " 的类型定义必须是 JSON 对象");
            assertEquals(type, node.path("type").asString());
        }
    }
}
