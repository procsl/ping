package cn.procsl.ping.boot.oa.domain.clock;

import java.util.Collection;

public interface HolidayService {

    Collection<MonthHolidayCalendar> getCalender(String yearAndMonth);


}
