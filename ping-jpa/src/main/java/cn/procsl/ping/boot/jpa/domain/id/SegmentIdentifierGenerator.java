package cn.procsl.ping.boot.jpa.domain.id;

import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Slf4j
public class SegmentIdentifierGenerator {

    final ConcurrentHashMap<String, SegmentLock> map = new ConcurrentHashMap<>();

    final IdentifierSegmentRepository repository;

    final int segmentSize;

    final int retryTimes;

    final long initValue;

    @Builder
    private SegmentIdentifierGenerator(@NonNull IdentifierSegmentRepository repository,
                                       int segmentSize, int retryTimes, long initValue) {

        if (segmentSize <= 0) {
            throw new IllegalArgumentException("segment size must be greater than 0");
        }

        if (initValue < 0) {
            throw new IllegalArgumentException("init value must be greater than 0");
        }

        if (retryTimes <= 0) {
            throw new IllegalArgumentException("retry times must be greater than 0");
        }

        this.repository = repository;
        this.segmentSize = segmentSize;
        this.initValue = initValue;
        this.retryTimes = retryTimes;
    }

    public Long nextId(String name) {

        SegmentLock current = this.map.get(name);
        if (current != null) {
            return current.nextValue();
        }

        // ID段回调
        Supplier<Long> supplier = () -> {
            for (int i = 0; i < this.retryTimes; i++) {
                Long next = this.nextSegmentValue(name, this.segmentSize, this.initValue);
                if (next == null) {
                    continue;
                }
                return next;
            }
            throw new IdentifierException("获取ID段超过限定次数: " + this.retryTimes);
        };

        // 不存在时调用回调函数
        this.map.computeIfAbsent(name, v -> new SegmentLock(this.segmentSize, supplier));
        return this.map.get(name).nextValue();
    }

    private final static class Segment {
        final private AtomicLong currentValue;
        final private long maxValue;


        public Segment(Long currentValue, long maxValue) {
            this.currentValue = new AtomicLong(currentValue);
            this.maxValue = maxValue;
        }

        public Long nextId() {
            long current = currentValue.getAndIncrement();
            if (current < maxValue) {
                return current;
            }
            return null;
        }

    }

    private static class SegmentLock {
        private final int segmentSize;
        private final Supplier<Long> supplier;
        private final ReentrantLock lock = new ReentrantLock();
        private final AtomicReference<Segment> reference = new AtomicReference<>();

        final static Segment empty = new Segment(0L, 0);

        public SegmentLock(int segmentSize, Supplier<Long> supplier) {
            this.segmentSize = segmentSize;
            this.supplier = supplier;
            this.reference.set(empty);
        }

        protected Long nextValue() {

            Long nextId = this.reference.get().nextId();
            if (nextId != null) {
                return nextId;
            }

            // TODO
            lock.lock();
            try {
                nextId = this.reference.get().nextId();
                if (nextId != null) {
                    return nextId;
                }

                Long startValue = supplier.get();
                Segment seg = new Segment(startValue, segmentSize + startValue);
                this.reference.set(seg);
            } finally {
                lock.unlock();
            }

            nextId = this.reference.get().nextId();
            if (nextId != null) {
                return nextId;
            }

            throw new IdentifierException("获取ID段不合法导致失败");
        }

    }

    /**
     * 获取下一个可分配的数据段
     *
     * @param segmentName 数据段名称
     * @param size        数据段大小
     * @param initValue   初始数据值
     * @return 返回可分配的数据段起始值, 如返回1, 则可分配数据段值为 1 至 1+size
     * @throws IdentifierException 如果分配重试次数超过指定值,或其他原因
     */
    protected Long nextSegmentValue(String segmentName, int size, Long initValue) {
        try {
            Optional<Long> mtp = repository.incrementBy(segmentName, size);
            if (Objects.requireNonNull(mtp).isPresent()) {
                return mtp.get();
            }
        } catch (IdentifierException e) {
            repository.save(segmentName, initValue + size);
            return initValue;
        } catch (RuntimeException e) {
            log.warn("获取ID段锁异常", e);
        }
        return null;
    }

}
