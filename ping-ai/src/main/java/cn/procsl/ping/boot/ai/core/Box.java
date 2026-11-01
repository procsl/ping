package cn.procsl.ping.boot.ai.core;

/**
 * 渲染数据的最小单元。
 *
 * <p>协议只认四个字段：{@code type} 开放、{@code id} 为会话内对象序号、
 * {@code seq} 为对象内片段序号、{@code content} 恒为字符串。</p>
 *
 * @param type    对象类型，消费端自行分派
 * @param id      对象 id，会话内从 0 递增且不复用
 * @param seq     片段序号；非负为片段，负数为该对象结束
 * @param content 不透明字符串
 */
public record Box(String type, int id, int seq, String content) {

    /** 结束片段的序号。 */
    public static final int END_SEQ = -1;

    /** 是否为对象结束片段。 */
    public boolean isEnd() {
        return seq < 0;
    }

    /**
     * 解析线上传来的 {@code seq}：只认非负整数，其余（缺失、非法、负数）一律视为结束。
     *
     * @param raw 原始值，可为 {@code null}
     * @return 非负片段序号，或 {@link #END_SEQ}
     */
    public static int parseSeq(String raw) {
        if (raw == null || raw.isBlank()) {
            return END_SEQ;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            return value < 0 ? END_SEQ : value;
        } catch (NumberFormatException e) {
            return END_SEQ;
        }
    }

    /** 结束片段。 */
    public static Box end(String type, int id) {
        return new Box(type, id, END_SEQ, "");
    }

    /** 完整对象的第一段。 */
    public static Box full(String type, int id, String content) {
        return new Box(type, id, 0, content == null ? "" : content);
    }
}
