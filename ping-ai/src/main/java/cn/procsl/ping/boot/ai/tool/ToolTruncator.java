package cn.procsl.ping.boot.ai.tool;

import java.util.Arrays;

/**
 * 工具结果截断：模型侧保留头尾并显式告知截断，展示侧只留摘要。
 */
public final class ToolTruncator {

    /** 回给模型的结果上限（字符）。 */
    public static final int MAX_MODEL_CHARS = 32 * 1024;
    /** 展示摘要上限（字符）。 */
    public static final int MAX_SUMMARY_CHARS = 2048;
    /** 展示摘要行数上限。 */
    public static final int MAX_SUMMARY_LINES = 40;

    /**
     * 截断回给模型的结果。
     *
     * @param result 原始结果
     * @return 可能带截断标记的结果
     */
    public String truncate(String result) {
        if (result == null) {
            return "";
        }
        if (result.length() <= MAX_MODEL_CHARS) {
            return result;
        }
        int head = MAX_MODEL_CHARS / 2;
        int tail = MAX_MODEL_CHARS - head;
        int omitted = result.length() - MAX_MODEL_CHARS;
        return result.substring(0, head)
                + "\n[已截断 " + omitted + " 字符]\n"
                + result.substring(result.length() - tail);
    }

    /**
     * 生成展示摘要。
     *
     * @param result 原始结果
     * @return 摘要
     */
    public String summary(String result) {
        if (result == null || result.isBlank()) {
            return "";
        }
        String[] lines = result.split("\\R");
        String joined = String.join("\n", Arrays.copyOf(lines, Math.min(lines.length, MAX_SUMMARY_LINES)));
        return joined.length() <= MAX_SUMMARY_CHARS ? joined : joined.substring(0, MAX_SUMMARY_CHARS);
    }
}
