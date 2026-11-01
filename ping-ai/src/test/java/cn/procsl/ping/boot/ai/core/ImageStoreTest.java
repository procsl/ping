package cn.procsl.ping.boot.ai.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageStoreTest {

    @TempDir
    Path temp;

    @Test
    void 按内容哈希落盘且不重复复制() throws Exception {
        Path source = temp.resolve("a.png");
        Files.write(source, new byte[]{1, 2, 3});
        ImageStore store = new ImageStore(temp.resolve("images"));

        ImageStore.StoredImage first = store.store(source);
        ImageStore.StoredImage second = store.store(source);

        assertThat(first.path()).isEqualTo(second.path());
        assertThat(first.path()).startsWith(temp.resolve("images"));
        try (var stream = Files.list(temp.resolve("images"))) {
            assertThat(stream).hasSize(1);
        }
    }

    @Test
    void 不同内容得到不同文件名() throws Exception {
        Path a = temp.resolve("a.png");
        Path b = temp.resolve("b.png");
        Files.write(a, new byte[]{1});
        Files.write(b, new byte[]{2});
        ImageStore store = new ImageStore(temp.resolve("images"));

        assertThat(store.store(a).name()).isNotEqualTo(store.store(b).name());
    }

    @Test
    void 不可读文件被拒绝() {
        ImageStore store = new ImageStore(temp.resolve("images"));

        assertThatThrownBy(() -> store.store(temp.resolve("missing.png")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("图片不可读");
    }
}
