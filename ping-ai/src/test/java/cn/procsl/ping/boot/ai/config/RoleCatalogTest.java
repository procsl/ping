package cn.procsl.ping.boot.ai.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RoleCatalogTest {

    @TempDir
    Path roles;

    @Test
    void 加载角色配置与提示词() throws Exception {
        Path dev = Files.createDirectories(roles.resolve("dev"));
        Files.writeString(dev.resolve("role.json"), """
                { "model": "claude_x", "tools": ["read_file", "grep"] }
                """);
        Files.writeString(dev.resolve("system.md"), "你是助手");
        Files.writeString(dev.resolve("first.md"), "介绍一下自己");

        RoleConfig role = new RoleCatalog(roles).find("dev").orElseThrow();

        assertThat(role.model()).isEqualTo("claude_x");
        assertThat(role.tools()).containsExactly("read_file", "grep");
        assertThat(role.systemPrompt()).isEqualTo("你是助手");
        assertThat(role.firstPrompt()).isEqualTo("介绍一下自己");
    }

    @Test
    void 提示词缺失时降级为空() throws Exception {
        Files.createDirectories(roles.resolve("dev"));

        RoleConfig role = new RoleCatalog(roles).find("dev").orElseThrow();

        assertThat(role.systemPrompt()).isNull();
        assertThat(role.firstPrompt()).isNull();
        assertThat(role.tools()).isEmpty();
    }

    @Test
    void role_json非法时按空配置继续() throws Exception {
        Path dev = Files.createDirectories(roles.resolve("dev"));
        Files.writeString(dev.resolve("role.json"), "not json");

        RoleConfig role = new RoleCatalog(roles).find("dev").orElseThrow();

        assertThat(role.model()).isNull();
        assertThat(role.tools()).isEmpty();
    }

    @Test
    void 角色名校验边界() {
        assertThat(RoleCatalog.isValidName("dev")).isTrue();
        assertThat(RoleCatalog.isValidName("my-role_2")).isTrue();
        assertThat(RoleCatalog.isValidName("")).isFalse();
        assertThat(RoleCatalog.isValidName("../etc")).isFalse();
        assertThat(RoleCatalog.isValidName("角色")).isFalse();
        assertThat(RoleCatalog.isValidName("my.role")).isFalse();
        assertThat(RoleCatalog.isValidName("my role")).isFalse();
        assertThat(RoleCatalog.isValidName("a".repeat(37))).isFalse();
    }

    @Test
    void 枚举时跳过非法目录() throws Exception {
        Files.createDirectories(roles.resolve("dev"));
        Files.createDirectories(roles.resolve("prod"));
        Files.createDirectories(roles.resolve("bad.name"));

        assertThat(new RoleCatalog(roles).list()).containsExactly("dev", "prod");
    }

    @Test
    void 未知角色返回空() {
        assertThat(new RoleCatalog(roles).find("missing")).isEmpty();
        assertThat(new RoleCatalog(roles).find("../etc")).isEmpty();
    }
}
