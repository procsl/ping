package cn.procsl.ping.boot.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class TraceIdGeneratorTest {

    @Test
    public void generateId() {
        TraceIdGenerator generator = TraceIdGenerator.initTraceId(4);
        {
            Long num = generator.calcMaxNum(4);
            log.info("num = {}", num);
            assertTrue(num > 999);
            assertTrue(num <= 9999);
        }

        {
            Long num = generator.calcMaxNum(5);
            log.info("num = {}", num);
            assertTrue(num > 9999);
            assertTrue(num <= 99999);
        }

        {
            Long num = generator.calcMaxNum(6);
            log.info("num = {}", num);
            assertTrue(num > 99999);
            assertTrue(num <= 999999);
        }

        {
            Long num = generator.calcMaxNum(7);
            log.info("num = {}", num);
            assertTrue(num > 999999);
            assertTrue(num <= 9999999);
        }

        for (int i = 10000; i > 0; i--) {
            String id = generator.generateId();
            log.info("id = {}", id);
        }

    }

}