package cn.procsl.ping.boot.ui;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 构建期聚合索引验证：断言 classpath 固定路径 META-INF/ping/ui/components.json
 * 存在、可解析，且包含 spec 列出的全部 16 个常用组件类型。
 *
 * <p>该测试依赖 process-classes 阶段已执行聚合脚本，因此只能经由 mvn test 触发
 * （surefire 晚于 process-classes），直接 javac 运行无效。
 */
class AggregatedIndexTest {

    static final String INDEX_PATH = "META-INF/ping/ui/components.json";

    private final JsonMapper mapper = JsonMapper.builder().build();

    private JsonNode readIndex() {
        ClassPathResource resource = new ClassPathResource(INDEX_PATH);
        assertTrue(resource.exists(),
            "缺少聚合索引 " + INDEX_PATH + "，请确认 gmavenplus 的 aggregate-ui-components 已绑定 process-classes");
        try (InputStream is = resource.getInputStream()) {
            JsonNode index = mapper.readTree(is);
            assertTrue(index.isObject(), "聚合索引必须是 JSON 对象");
            return index;
        } catch (Exception e) {
            throw new IllegalStateException("聚合索引无法解析：" + INDEX_PATH, e);
        }
    }

    @Test
    void indexIsPresentAndParseable() {
        JsonNode index = readIndex();
        assertEquals(1, index.path("schema_version").asInt(),
            "聚合索引的 schema_version 应为 1");
        assertTrue(index.path("types").isArray(), "聚合索引缺少 types 数组");
        assertTrue(index.path("pages").isArray(), "聚合索引缺少 pages 数组");
    }

    @Test
    void indexContainsAllSixteenSpecTypes() {
        JsonNode index = readIndex();

        Set<String> actual = new HashSet<>();
        index.path("types").forEach(type -> actual.add(type.path("type").asString()));

        Set<String> missing = new HashSet<>(ComponentTypeDefinitionTest.SPEC_TYPES);
        missing.removeAll(actual);
        assertTrue(missing.isEmpty(), "聚合索引缺少 spec 声明的组件类型：" + missing);
        assertTrue(actual.containsAll(ComponentTypeDefinitionTest.SHELL_TYPES),
            "聚合索引缺少框架壳体类型");
    }

    @Test
    void indexTypesAreUniqueAndWellFormed() {
        JsonNode index = readIndex();

        Set<String> seen = new HashSet<>();
        for (JsonNode type : index.path("types")) {
            String key = type.path("type").asString();
            assertTrue(key.trim().length() > 0, "聚合索引存在缺少 type 的条目");
            assertTrue(seen.add(key), "聚合索引中类型重复：" + key);
            assertTrue(type.path("description").asString().length() > 0,
                key + " 缺少语义说明");
        }
    }
}
