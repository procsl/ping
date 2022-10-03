package cn.procsl.ping.boot.oa.domain.clock;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class EmployeeClockService {

    public Collection<EmployeeClockCalendar> build(@NonNull Collection<EmployeeClock> clocks,
                                                   @NonNull Collection<MonthHolidayCalendar> calendars) {

        Map<String, List<EmployeeClock>> group = clocks.stream()
                                                       .collect(Collectors.groupingBy(EmployeeClock::getClockDay));


        Collection<EmployeeClockCalendar> list = new ArrayList<>(clocks.size());
        for (MonthHolidayCalendar calendar : calendars) {

            EmployeeClock clock = null;
            val target = group.get(calendar.getDay());
            if (target != null && !target.isEmpty()) {
                clock = target.get(0);
            }
            EmployeeClockCalendar tmp = new EmployeeClockCalendar(calendar, clock);
            list.add(tmp);
        }

        return list;
    }

}
