package cn.procsl.ping.boot.jpa.domain.id;

import cn.procsl.ping.boot.jpa.support.IdentifierGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RequiredArgsConstructor
public class TableIdentifierGenerator implements IdentifierGenerator<Long> {


    final ConcurrentHashMap<String, Segment> map = new ConcurrentHashMap<>();

    final IdentifierSegmentRepository repository;

    final TransactionTemplate transactionTemplate;

    final int segmentSize;

    final long initialValue;

    final int retryTimes = 5;

    @Override
    public Long nextId(String name, Long initId) {

        for (int i = 0; i < retryTimes; i++) {

            Segment current = map.get(name);
            if (current == null) {
                Long value = this.nextSegmentValue(name,
                        segmentSize, initialValue);
                current = map.putIfAbsent(name, new Segment(value, segmentSize));
            }

            if (current == null) {
                continue;
            }

            Long currentValue = current.nextValue();
            if (currentValue != null) {
                return currentValue;
            }

            // 当超出范围时, 删除超出范围段
            map.remove(name);
        }

        throw new IdentifierException("获取ID超过指定次数");

    }

    private static class Segment {
        final AtomicLong currentValue;
        final Long maxValue;

        public Segment(Long startValue, int segmentSize) {
            this.currentValue = new AtomicLong(startValue);
            this.maxValue = segmentSize + startValue;
        }

        public Long nextValue() {
            long current = currentValue.getAndIncrement();
            if (current > maxValue) {
                return null;
            }
            return current;
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
    protected Long nextSegmentValue(String segmentName, int size, Long initValue) throws IdentifierException {

        for (int i = 0; i < retryTimes; i++) {
            try {
                return transactionTemplate.execute(status -> {

                    Optional<Long> optional = repository.incrementBy(segmentName, size);
                    return optional.orElseGet(() -> {
                        log.debug("保存: {}, {}", segmentName, initValue);
                        repository.save(segmentName, initValue);
                        status.flush();
                        return initValue;
                    });

                });
            } catch (RuntimeException e) {
                log.warn("获取ID段出现异常", e);
            }
        }
        throw new IdentifierException("获取ID段失败, 超过重试次数");
    }


}
