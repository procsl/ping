package cn.procsl.ping.boot.ai.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * 消费端 box 归组与缺口检测。
 *
 * <p>按对象 {@code id} 归组，按 {@code seq} 拼接；发现对象 id 或片段序号缺口时
 * 回调 {@code onGap}，由调用方发起重放。状态只在内存中，断线后 {@link #reset()} 即可。</p>
 */
public final class BoxTracker {

    private final Map<Integer, TrackedObject> objects = new TreeMap<>();
    private final Consumer<Integer> onGap;
    private final Consumer<Integer> onAck;
    private int expected;

    public BoxTracker(Consumer<Integer> onGap, Consumer<Integer> onAck) {
        this.onGap = onGap == null ? ignored -> { } : onGap;
        this.onAck = onAck == null ? ignored -> { } : onAck;
    }

    /**
     * 处理一个 box。
     *
     * @param box 收到的 box
     */
    public void onBox(Box box) {
        if (box.id() > expected) {
            onGap.accept(expected);
            return;
        }
        TrackedObject existing = objects.get(box.id());
        if (box.id() < expected && existing != null && !existing.open) {
            return;
        }
        onAck.accept(box.id());

        if (box.isEnd()) {
            if (existing != null) {
                existing.open = false;
            }
            return;
        }
        if (box.seq() == 0) {
            objects.put(box.id(), new TrackedObject(box.type(), box.content()));
            expected = Math.max(expected, box.id() + 1);
            return;
        }
        if (existing == null || !existing.open || box.seq() != existing.lastSeq + 1) {
            objects.remove(box.id());
            onGap.accept(box.id());
            return;
        }
        existing.content.append(box.content());
        existing.lastSeq = box.seq();
    }

    /** 下一个期望的新对象 id。 */
    public int expected() {
        return expected;
    }

    /** 当前对象快照，按 id 升序。 */
    public List<TrackedObject> objects() {
        return new ArrayList<>(objects.values());
    }

    /** 断线清理：丢弃全部本地状态，重连后应全量重放。 */
    public void reset() {
        objects.clear();
        expected = 0;
    }

    /** 消费端对象状态。 */
    public static final class TrackedObject {

        private final String type;
        private final StringBuilder content;
        private int lastSeq;
        private boolean open = true;

        private TrackedObject(String type, String content) {
            this.type = type;
            this.content = new StringBuilder(content == null ? "" : content);
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
    }
}
