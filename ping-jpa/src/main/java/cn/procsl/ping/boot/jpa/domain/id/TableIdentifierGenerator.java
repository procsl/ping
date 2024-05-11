package cn.procsl.ping.boot.jpa.support;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
class TableIdentifierGenerator implements IdentifierGenerator<Long> {


    final ConcurrentHashMap<String, Segment> map = new ConcurrentHashMap<>();

    final IdentifierSegmentService identifierSegmentService;

    final int segmentSize;

    final Long initialValue;

    final int retryTimes = 5;

    @Override
    public Long nextId(String name, Long initId) {

        for (int i = 0; i < retryTimes; i++) {

            Segment current = map.get(name);
            if (current == null) {
                Long value = this.identifierSegmentService.nextSegmentValue(name,
                        segmentSize, initialValue, retryTimes);
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

}
