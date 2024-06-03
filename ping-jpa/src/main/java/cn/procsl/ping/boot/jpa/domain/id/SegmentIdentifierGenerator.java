package cn.procsl.ping.boot.jpa.domain.id;

import cn.procsl.ping.boot.common.utils.IdentifierGenerator;
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
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class TableIdentifierGenerator implements IdentifierGenerator<Long> {


    final ConcurrentHashMap<String, SegmentLock> map = new ConcurrentHashMap<>();

    final IdentifierSegmentRepository repository;

    final TransactionTemplate transactionTemplate;

    final int segmentSize;

    final int retryTimes;

    public TableIdentifierGenerator(IdentifierSegmentRepository repository, PlatformTransactionManager transactionManager, int segmentSize) {
        this(repository, transactionManager, segmentSize, 5);
    }

    public TableIdentifierGenerator(IdentifierSegmentRepository repository, PlatformTransactionManager transactionManager, int segmentSize, int retryTimes) {
        this.repository = repository;
        this.segmentSize = segmentSize;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        def.setReadOnly(false);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        def.setTimeout(1000);
        this.transactionTemplate = new TransactionTemplate(transactionManager, def);
        this.retryTimes = retryTimes;
    }

    @Override
    public Long nextId(String name, Long initId) {

        SegmentLock current = map.get(name);
        if (current != null) {
            return current.nextValue();
        }

        // ID段回调
        Supplier<Long> supplier = () -> {
            for (int i = 0; i < retryTimes; i++) {
                Long next = this.nextSegmentValue(name, segmentSize, initId);
                if (next == null) {
                    continue;
                }
                return next;
            }
            throw new IdentifierException("获取ID段超过限定次数: " + retryTimes);
        };

        // 不存在时调用回调函数
        map.computeIfPresent(name, (v, o) -> new SegmentLock(segmentSize, supplier));
        return map.get(name).nextValue();
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

            synchronized (this) {
                nextId = this.reference.get().nextId();
                if (nextId != null) {
                    return nextId;
                }

                Long startValue = supplier.get();
                Segment seg = new Segment(startValue, segmentSize + startValue);
                this.reference.set(seg);
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
            Optional<Long> mtp = transactionTemplate.execute(status -> repository.incrementBy(segmentName, size));
            if (Objects.requireNonNull(mtp).isPresent()) {
                return mtp.get();
            }
        } catch (IdentifierException e) {
            transactionTemplate.execute(status -> {
                repository.save(segmentName, initValue + size);
                status.flush();
                return null;
            });
            return initValue;
        } catch (RuntimeException e) {
            log.warn("获取ID段锁异常", e);
        }
        return null;
    }

}
