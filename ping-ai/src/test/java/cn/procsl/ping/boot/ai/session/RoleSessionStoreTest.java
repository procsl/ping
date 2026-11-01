package cn.procsl.ping.boot.ai.session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoleSessionStoreTest {

    @TempDir
    Path roleDir;

    @Test
    void 保存后可以读回() {
        RoleSessionStore store = new RoleSessionStore(roleDir);
        SessionFile file = new SessionFile("S1", "dev", "gpt",
                List.of(MessageRecord.user("hi"), MessageRecord.assistant("hello")));

        store.save(file);

        assertThat(store.exists("S1")).isTrue();
        SessionFile loaded = store.load("S1").orElseThrow();
        assertThat(loaded.sessionId()).isEqualTo("S1");
        assertThat(loaded.messages()).hasSize(2);
        assertThat(loaded.messages().getFirst().text()).isEqualTo("hi");
    }

    @Test
    void 写入不留下临时文件() throws Exception {
        RoleSessionStore store = new RoleSessionStore(roleDir);
        store.save(new SessionFile("S1", "dev", "gpt", List.of(MessageRecord.user("hi"))));

        try (var stream = Files.list(roleDir.resolve("sessions"))) {
            assertThat(stream.map(path -> path.getFileName().toString()))
                    .containsExactly("S1.json");
        }
    }

    @Test
    void 不同角色目录互相隔离() {
        RoleSessionStore first = new RoleSessionStore(roleDir.resolve("a"));
        RoleSessionStore second = new RoleSessionStore(roleDir.resolve("b"));

        first.save(new SessionFile("S1", "a", "gpt", List.of(MessageRecord.user("hi"))));

        assertThat(first.exists("S1")).isTrue();
        assertThat(second.exists("S1")).isFalse();
    }

    @Test
    void 损坏文件按空会话处理() throws Exception {
        RoleSessionStore store = new RoleSessionStore(roleDir);
        Path sessions = Files.createDirectories(roleDir.resolve("sessions"));
        Files.writeString(sessions.resolve("S1.json"), "not json");

        assertThat(store.load("S1")).isEmpty();
    }
}
