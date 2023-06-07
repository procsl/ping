package cn.procsl.ping.boot.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class TraceIdGenerator {

    private final AtomicLong init;
    private final String fmt;
    private String startTime;
    private final int len;

    private final Long max;

    private TraceIdGenerator(String fmt, int len) {
        if (len <= 3) {
            throw new IllegalArgumentException("len 至少为 4");
        }
        if (len > 18) {
            throw new IllegalArgumentException("len 最大值为 19");
        }

        this.init = new AtomicLong(0);
        this.startTime = new SimpleDateFormat(fmt).format(new Date()) + "%0" + len + "d";
        this.len = len;

        // 取指定位数的近似最大值, 预留部分防止并发问题
        this.max = calcMaxNum(len);
        this.fmt = fmt;
    }

    protected Long calcMaxNum(int len) {
        return (long) (Math.pow(10, len) * 0.95);
    }


    public String generateId() {
        ifNeedReset();
        return String.format(startTime, next());
    }

    protected void ifNeedReset() {
        if (this.init.get() >= max) {
            this.startTime = new SimpleDateFormat(fmt).format(new Date()) + "%0" + len + "d";
            this.init.set(0);
        }
    }

    protected Number next() {
        return init.getAndIncrement();
    }

    public static TraceIdGenerator initTraceId(int len) {
        return new TraceIdGenerator("yyyyMMddHHmmss", len);
    }

    public static TraceIdGenerator initTraceId(String fmt, int len) {
        return new TraceIdGenerator(fmt, len);
    }

}
