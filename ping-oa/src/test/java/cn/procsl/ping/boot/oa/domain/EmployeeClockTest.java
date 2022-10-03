package cn.procsl.ping.boot.oa.domain;

import cn.procsl.ping.boot.oa.domain.clock.EmployeeClock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class EmployeeClockTest {

    @Test
    public void autoSign() {
        EmployeeClock clock = new EmployeeClock("test");
        clock.autoSign(9, 18);
        log.info("Test:{}", clock);
    }
}