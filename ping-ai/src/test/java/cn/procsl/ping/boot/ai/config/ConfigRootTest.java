package cn.procsl.ping.boot.ai.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigRootTest {

    @TempDir
    Path temp;

    @Test
    void 默认路径指向用户主目录下的_lwl() {
        Path root = ConfigRoot.resolve(null).root();
        assertThat(root.getFileName().toString()).isEqualTo(ConfigRoot.DEFAULT_DIRECTORY);
        assertThat(root.getParent()).isEqualTo(Path.of(System.getProperty("user.home")));
    }

    @Test
    void 支持命令行覆盖路径() {
        Path custom = temp.resolve("custom");
        assertThat(ConfigRoot.resolve(custom.toString()).root()).isEqualTo(custom);
    }

    @Test
    void 初始化创建目录骨架并可写() {
        ConfigRoot root = ConfigRoot.resolve(temp.resolve("cfg").toString()).initialize();

        assertThat(root.writable()).isTrue();
        assertThat(root.logs()).isDirectory();
        assertThat(root.images()).isDirectory();
        assertThat(root.roles()).isDirectory();
        assertThat(root.configFile()).isEqualTo(root.root().resolve("config.json"));
    }

    @Test
    void 目录不可创建时降级不抛异常() throws Exception {
        Path file = temp.resolve("blocker");
        Files.writeString(file, "x");

        ConfigRoot root = ConfigRoot.resolve(file.resolve("sub").toString()).initialize();

        assertThat(root.writable()).isFalse();
    }
}
