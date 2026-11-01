package cn.procsl.ping.boot.ai.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * 角色目录下的会话仓库：{@code roles/<name>/sessions/<session-id>.json}。
 *
 * <p>写入采用「临时文件 + 原子替换」，避免流式过程中进程中断留下半个 JSON。
 * 读取失败只告警并返回空，不中断启动。</p>
 */
public final class RoleSessionStore implements SessionStore {

    private static final Logger log = LoggerFactory.getLogger(RoleSessionStore.class);

    private final Path sessionsDirectory;
    private final ObjectMapper mapper = new ObjectMapper();

    public RoleSessionStore(Path roleDirectory) {
        this.sessionsDirectory = roleDirectory.resolve("sessions");
    }

    @Override
    public void save(SessionFile file) {
        try {
            Files.createDirectories(sessionsDirectory);
            Path target = path(file.sessionId());
            Path temp = Files.createTempFile(sessionsDirectory, file.sessionId(), ".tmp");
            Files.writeString(temp, mapper.writeValueAsString(file));
            move(temp, target);
        } catch (Exception e) {
            log.warn("会话保存失败: {} ({})", file.sessionId(), e.getMessage());
        }
    }

    @Override
    public Optional<SessionFile> load(String sessionId) {
        Path file = path(sessionId);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.readValue(Files.readString(file), SessionFile.class));
        } catch (Exception e) {
            log.warn("会话读取失败，按空会话处理: {} ({})", sessionId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean exists(String sessionId) {
        return Files.isRegularFile(path(sessionId));
    }

    private Path path(String sessionId) {
        return sessionsDirectory.resolve(sessionId + ".json");
    }

    private void move(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
