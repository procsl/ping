package cn.procsl.ping.boot.oa.adapter;

import cn.procsl.ping.boot.oa.TestOaApplication;
import cn.procsl.ping.boot.oa.domain.clock.HolidayService;
import cn.procsl.ping.boot.oa.domain.clock.MonthHolidayCalendar;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.inject.Inject;
import java.util.Collection;


@Slf4j
@SpringBootTest(classes = TestOaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ApiHubCalendarServiceAdapterTest {

    @Inject
    HolidayService adapter;

    @Test
    public void getCalender() {
        Collection<MonthHolidayCalendar> list = adapter.getCalender("202210");
        log.info("Calender:{}", list);
    }
}