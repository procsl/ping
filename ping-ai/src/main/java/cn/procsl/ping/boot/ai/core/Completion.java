package cn.procsl.ping.boot.ai.core;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 一轮对话的完成对象。
 *
 * <p>{@code submit} 立即返回本对象：订阅者持续收到本轮的 box，直到轮次结束。
 * 订阅晚于产出时，先补发已缓冲的 box，再接收后续增量。</p>
 */
public final class Completion {

    private final List<Box> buffer = new ArrayList<>();
    private final List<Consumer<Box>> subscribers = new ArrayList<>();
    private final CountDownLatch finished = new CountDownLatch(1);
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final Runnable cancelAction;

    public Completion(Runnable cancelAction) {
        this.cancelAction = cancelAction == null ? () -> { } : cancelAction;
    }

    /**
     * 订阅本轮的 box。已产出的内容会先补发。
     *
     * @param subscriber 消费端
     */
    public void subscribe(Consumer<Box> subscriber) {
        List<Box> replay;
        synchronized (this) {
            replay = List.copyOf(buffer);
            subscribers.add(subscriber);
        }
        replay.forEach(subscriber);
    }

    /**
     * 阻塞等待本轮结束。
     *
     * @param timeout 最长等待时间
     * @return 是否已结束
     */
    public boolean await(Duration timeout) {
        try {
            return timeout == null || timeout.isNegative() || timeout.isZero()
                    ? finished.getCount() == 0
                    : finished.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /** 取消本轮；重复调用无副作用。 */
    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            cancelAction.run();
        }
    }

    public boolean cancelled() {
        return cancelled.get();
    }

    /** 投递一条 box（内部使用）。 */
    void deliver(Box box) {
        List<Consumer<Box>> targets;
        synchronized (this) {
            buffer.add(box);
            targets = List.copyOf(subscribers);
        }
        targets.forEach(subscriber -> subscriber.accept(box));
    }

    /** 标记本轮结束（内部使用）。 */
    void finish() {
        finished.countDown();
    }

    /** 轮次是否已结束。 */
    public boolean isFinished() {
        return finished.getCount() == 0;
    }
}
