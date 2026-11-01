package cn.procsl.ping.boot.ai.core;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

/**
 * 会话内对象表。
 *
 * <p>对象表是渲染投影：不落盘，由 {@code ObjectProjector} 从消息表重建。
 * 实时推送与全量重建共用同一套 id 分配与 seq 递增规则。</p>
 *
 * <p>线程安全由调用方（{@code Session}）保证。</p>
 */
public final class ObjectTable {

    private final NavigableMap<Integer, Entry> entries = new TreeMap<>();
    private int nextId;

    /**
     * 分配一个新对象并标记为打开。
     *
     * @param type 对象类型
     * @return 对象 id
     */
    public int start(String type) {
        return start(type, false);
    }

    /**
     * 分配一个新对象并标记为打开。
     *
     * @param type    对象类型
     * @param handled 是否已处理（仅错误对象）
     * @return 对象 id
     */
    public int start(String type, boolean handled) {
        int id = nextId++;
        entries.put(id, new Entry(id, type, handled));
        return id;
    }

    /**
     * 追加一段内容。
     *
     * @param id       对象 id
     * @param fragment 片段内容
     * @return 带递增 {@code seq} 的 box
     */
    public Box push(int id, String fragment) {
        Entry entry = require(id);
        if (!entry.open) {
            throw new IllegalStateException("对象已结束: " + id);
        }
        int seq = ++entry.lastSeq;
        String text = fragment == null ? "" : fragment;
        entry.content.append(text);
        return new Box(entry.type, id, seq, text);
    }

    /**
     * 结束一个对象。
     *
     * @param id 对象 id
     * @return 结束片段
     */
    public Box finish(int id) {
        Entry entry = require(id);
        entry.open = false;
        return Box.end(entry.type, id);
    }

    /**
     * 生成完整对象：一段内容 + 结束。
     *
     * @param type    对象类型
     * @param content 完整内容
     * @return 两条 box
     */
    public List<Box> complete(String type, String content) {
        return complete(type, content, false);
    }

    /**
     * 生成完整对象：一段内容 + 结束。
     *
     * @param type    对象类型
     * @param content 完整内容
     * @param handled 是否已处理（仅错误对象）
     * @return 两条 box
     */
    public List<Box> complete(String type, String content, boolean handled) {
        int id = start(type, handled);
        Box first = push(id, content);
        return List.of(first, finish(id));
    }

    /**
     * 标记错误对象为已处理：重放时带 handled 标记。
     *
     * @param id 对象 id
     */
    public void markHandled(int id) {
        require(id).handled = true;
    }

    /**
     * 用投影结果填充对象表（加载会话时重建）。
     *
     * @param projected 按 id 升序的对象
     */
    public void seed(List<ProjectedObject> projected) {
        for (ProjectedObject object : projected) {
            if (object.id() != nextId) {
                throw new IllegalStateException("投影对象 id 不连续: 期望 " + nextId + " 实际 " + object.id());
            }
            complete(object.type(), object.content(), object.handled());
        }
    }

    /** 是否存在未结束对象。 */
    public boolean hasOpen() {
        return entries.values().stream().anyMatch(entry -> entry.open);
    }

    /** 全部未结束对象的 id。 */
    public List<Integer> openIds() {
        return entries.values().stream().filter(entry -> entry.open).map(entry -> entry.id).toList();
    }

    public boolean isOpen(int id) {
        Entry entry = entries.get(id);
        return entry != null && entry.open;
    }

    public Optional<Entry> get(int id) {
        return Optional.ofNullable(entries.get(id));
    }

    /** 下一个待分配的 id。 */
    public int nextId() {
        return nextId;
    }

    /**
     * 按 id 升序重放，每个对象两条 box（完整内容 + 结束）。
     *
     * @param fromId 起始 id（含）
     * @return 重放序列
     */
    public List<Box> history(int fromId) {
        List<Box> boxes = new ArrayList<>();
        entries.tailMap(fromId, true).values().forEach(entry -> {
            String content = entry.handled ? withHandled(entry.content.toString()) : entry.content.toString();
            boxes.add(Box.full(entry.type, entry.id, content));
            boxes.add(Box.end(entry.type, entry.id));
        });
        return List.copyOf(boxes);
    }

    /**
     * 已处理的错误对象在重放时带 {@code handled} 标记。
     *
     * @param content 原始内容
     * @return 带标记的内容
     */
    static String withHandled(String content) {
        int end = content.lastIndexOf('}');
        if (end >= 0 && content.trim().startsWith("{")) {
            return content.substring(0, end) + ",\"handled\":true" + content.substring(end);
        }
        return content + "\n[handled]";
    }

    /** 全部对象的只读快照，按 id 升序。 */
    public List<Entry> snapshot() {
        return List.copyOf(entries.values());
    }

    private Entry require(int id) {
        Entry entry = entries.get(id);
        if (entry == null) {
            throw new IllegalArgumentException("对象不存在: " + id);
        }
        return entry;
    }

    /** 对象表中的一条记录。 */
    public static final class Entry {

        private final int id;
        private final String type;
        private final StringBuilder content = new StringBuilder();
        private boolean handled;
        private int lastSeq = -1;
        private boolean open = true;

        private Entry(int id, String type, boolean handled) {
            this.id = id;
            this.type = type;
            this.handled = handled;
        }

        public int id() {
            return id;
        }

        public String type() {
            return type;
        }

        public String content() {
            return content.toString();
        }

        public int lastSeq() {
            return lastSeq;
        }

        public boolean open() {
            return open;
        }

        public boolean handled() {
            return handled;
        }
    }
}
