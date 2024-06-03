package cn.procsl.ping.boot.common.utils;

import java.util.concurrent.atomic.AtomicLong;

public class SimpleLongIdGenerator implements IdentifierGenerator<Long> {

    AtomicLong atomicLong = new AtomicLong(System.currentTimeMillis());

    @Override
    public Long nextId() {
        return atomicLong.getAndIncrement();
    }

}
