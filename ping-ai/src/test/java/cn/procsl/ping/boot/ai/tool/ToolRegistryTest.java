package cn.procsl.ping.boot.ai.tool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ToolRegistryTest {

    @TempDir
    Path temp;

    @Test
    void 注册后可按名查询与取用() {
        ToolRegistry registry = BuiltinTools.registry();

        assertThat(registry.query(ToolQuery.all())).extracting(ToolDescriptor::name)
                .containsExactly("read_file", "grep");
        assertThat(registry.find("read_file")).isNotNull();
        assertThat(registry.find("missing")).isNull();
    }

    @Test
    void 未注册的名字被忽略() {
        ToolRegistry registry = BuiltinTools.registry();

        assertThat(registry.query(new ToolQuery(List.of("read_file", "missing"))))
                .extracting(ToolDescriptor::name).containsExactly("read_file");
    }

    @Test
    void 三段式优先会话指定再角色默认() {
        ToolResolver resolver = new ToolResolver(BuiltinTools.registry());

        assertThat(resolver.resolve(List.of("grep"), List.of("read_file")))
                .extracting(ToolDescriptor::name).containsExactly("grep");
        assertThat(resolver.resolve(List.of(), List.of("read_file")))
                .extracting(ToolDescriptor::name).containsExactly("read_file");
        assertThat(resolver.resolve(null, null)).isEmpty();
    }

    @Test
    void 内置工具可执行() throws Exception {
        Path file = temp.resolve("a.txt");
        Files.writeString(file, "hello\nworld\n");
        ToolRegistry registry = BuiltinTools.registry();

        String read = registry.find("read_file").call("{\"path\":\"" + file + "\"}");
        assertThat(read).contains("hello");

        String grep = registry.find("grep")
                .call("{\"pattern\":\"wor\",\"path\":\"" + temp + "\"}");
        assertThat(grep).contains("world");
    }
}
