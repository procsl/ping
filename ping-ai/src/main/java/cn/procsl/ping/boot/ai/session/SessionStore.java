package cn.procsl.ping.boot.ai.session;

import java.util.Optional;

/**
 * 会话存储端口。
 */
public interface SessionStore {

    /**
     * 保存会话（临时文件 + 原子替换）。
     *
     * @param file 会话内容
     */
    void save(SessionFile file);

    /**
     * 读取会话。
     *
     * @param sessionId 会话标识
     * @return 不存在或损坏时为空
     */
    Optional<SessionFile> load(String sessionId);

    /** 会话是否已存在。 */
    boolean exists(String sessionId);
}
