package cn.procsl.ping.boot.ui.component;

import cn.procsl.ping.boot.ui.TestUiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 装配一次性验证（spec：启动后一次性装配进内存注册表）——
 * 装配完成后，无论是来源对象还是 classpath 中的描述文件被改动，注册表内容都不再变化，
 * 需重启应用才能反映。
 */
@SpringBootTest(
    classes = TestUiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UIComponentRegistryTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Autowired
    UIComponentRegistry registry;

    private JsonNode json(String source) {
        try {
            return mapper.readTree(source);
        } catch (Exception e) {
            throw new IllegalStateException("测试夹具 JSON 无法解析", e);
        }
    }

    /** 连续多次取快照内容一致 */
    @Test
    void repeatedSnapshotsAreIdentical() {
        assertEquals(registry.snapshot(), registry.snapshot(),
            "重复请求的响应内容必须一致");
        assertEquals(registry.snapshot().toString(), registry.snapshot().toString());
    }

    /** 快照本身不可变，外部无法通过列表操作污染注册表 */
    @Test
    void snapshotIsImmutable() {
        assertThrows(UnsupportedOperationException.class, () -> registry.types().add(null));
        assertThrows(UnsupportedOperationException.class, () -> registry.pages().add(null));
    }

    /** 装配时深拷贝：装配完成后修改来源对象不影响注册表 */
    @Test
    void laterMutationOfSourceDoesNotLeakIntoRegistry() {
        ObjectNode tree = (ObjectNode) json("""
            {"type": "application", "id": "src.app", "name": "原始名称"}
            """);
        ObjectNode index = mapper.createObjectNode();
        index.set("types", json("[]"));
        index.set("pages", json("[{\"key\": \"p\"}]"));
        ((ObjectNode) index.path("pages").get(0)).set("tree", tree);

        UIComponentRegistry target = new UIComponentRegistry(null);
        target.assemble(index, null, Map.of());

        String before = target.snapshot().toString();
        tree.put("name", "装配后被改的名字");
        tree.put("id", "src.app.changed");

        assertEquals(before, target.snapshot().toString(),
            "装配后修改来源不得影响注册表内容");
        assertEquals("原始名称", target.pages().get(0).path("name").asString());
    }

    /** 装配完成后，classpath 中的描述文件被改动也不影响已装配的注册表（写入后立即还原） */
    @Test
    void laterChangeOnClasspathDoesNotAffectAssembledRegistry() throws Exception {
        ClassPathResource resource = new ClassPathResource(UIComponentRegistry.INDEX_PATH);
        assertTrue(resource.exists(), "需要构建期聚合索引已生成");

        Path file = Path.of(resource.getURI());
        byte[] original = Files.readAllBytes(file);
        String before = registry.snapshot().toString();

        try {
            Files.writeString(file,
                "{\"schema_version\":1,\"types\":[],\"pages\":[]}",
                StandardCharsets.UTF_8);
            assertEquals(before, registry.snapshot().toString(),
                "启动后修改 classpath 描述不得改变注册表内容，需重启应用才能反映");
        } finally {
            Files.write(file, original);
        }

        assertEquals(before, registry.snapshot().toString(), "还原后注册表仍应保持原内容");
    }
}
