package cn.procsl.ping.boot.ai.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 对话相关全局设置（{@code config.json.chat}）。
 *
 * @param maxHistory 会话历史窗口上限，全局统一
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatSettings(@JsonProperty("max_history") Integer maxHistory) {

    /** 默认历史窗口。 */
    public static final int DEFAULT_MAX_HISTORY = 40;

    public ChatSettings {
        maxHistory = maxHistory == null || maxHistory <= 0 ? DEFAULT_MAX_HISTORY : maxHistory;
    }

    public static ChatSettings defaults() {
        return new ChatSettings(DEFAULT_MAX_HISTORY);
    }
}
