package cn.procsl.ping.boot.ai.upstream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

/**
 * 上游消息解析：按 {@code type} 分派，未知类型忽略并记录日志。
 */
public final class UpstreamMessages {

    private static final Logger log = LoggerFactory.getLogger(UpstreamMessages.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private UpstreamMessages() {
    }

    /**
     * 解析一条上游消息。
     *
     * @param json 原始 JSON
     * @return 无法识别或解析失败时为空
     */
    public static Optional<UpstreamMessage> parse(String json) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(MAPPER.readValue(json, UpstreamMessage.class));
        } catch (Exception e) {
            log.warn("忽略无法识别的上游消息: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
