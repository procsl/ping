package cn.procsl.ping.boot.oa.domain.clock;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class MonthHolidayCalendar implements Serializable {

    /**
     * 日期
     */
    String day;


    /**
     * 是否周末
     */
    String weekend;

    /**
     * 是否工作日
     */
    String workday;

    /**
     * 是否为节假日当天
     */
    String holidayToday;

    /**
     * 是否为法定节假日
     */
    String holidayLegal;

    /**
     * 是否为假期节假日
     */
    String holidayRecess;

}
