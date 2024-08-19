package cn.procsl.ping.boot.jpa.domain.id;

import cn.procsl.ping.boot.common.utils.IdentifierGenerator;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Slf4j
public class SegmentIdentifierGenerator implements IdentifierGenerator<Long> {


    final ConcurrentHashMap<String, SegmentLock> map = new ConcurrentHashMap<>();


    final IdentifierSegmentRepository repository;

    final int segmentSize;

    final int retryTimes;

    final String name;

    final long initValue;


    @Builder
    private SegmentIdentifierGenerator(@NonNull IdentifierSegmentRepository repository,
                                       int segmentSize, int retryTimes, @NonNull String name, long initValue) {

        if (segmentSize <= 0) {
            throw new IllegalArgumentException("segmentSize must be greater than 0");
        }

        if (initValue < 0) {
            throw new IllegalArgumentException("init value must be greater than 0");
        }

        if (retryTimes <= 0) {
            throw new IllegalArgumentException("retryTimes must be greater than 0");
        }

        if (name.isEmpty()) {
            throw new IllegalArgumentException("name must not be empty");
        }

        this.name = name;
        this.repository = repository;
        this.segmentSize = segmentSize;
        this.initValue = initValue;
        this.retryTimes = retryTimes;
    }

    @Override
    public Long nextId() {

        SegmentLock current = this.map.get(this.name);
        if (current != null) {
            return current.nextValue();
        }

        // ID段回调
        Supplier<Long> supplier = () -> {
            for (int i = 0; i < this.retryTimes; i++) {
                Long next = this.nextSegmentValue(this.name, this.segmentSize, this.initValue);
                if (next == null) {
                    continue;
                }
                return next;
            }
            throw new IdentifierException("获取ID段超过限定次数: " + this.retryTimes);
        };

        // 不存在时调用回调函数
        this.map.computeIfAbsent(this.name, v -> new SegmentLock(this.segmentSize, supplier));
        return this.map.get(this.name).nextValue();
    }

    private static class Segment {
        final private AtomicLong currentValue;
        final private long maxValue;

        public Segment(Long currentValue, long maxValue) {
            this.currentValue = new AtomicLong(currentValue);
            this.maxValue = maxValue;
        }

        protected Long nextId() {
            long current = currentValue.getAndIncrement();
            if (current < maxValue) {
                return current;
            }
            return null;
        }

    }

    private static class SegmentLock {
        final int segmentSize;
        final Supplier<Long> supplier;
        private final ReentrantLock lock = new ReentrantLock();
        final AtomicReference<Segment> reference = new AtomicReference<>();

        public SegmentLock(int segmentSize, Supplier<Long> supplier) {
            this.segmentSize = segmentSize;
            this.supplier = supplier;
            this.reference.set(new Segment(0L, 0));
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
