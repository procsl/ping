package cn.procsl.ping.boot.oa.web.clock;

import cn.procsl.ping.boot.oa.adapter.EmployeeServiceAdapter;
import cn.procsl.ping.boot.oa.domain.clock.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Tag(name = "employee-clock", description = "员工打卡")
public class EmployeeClockController {

    final JpaRepository<EmployeeClock, Long> repository;

    final JpaSpecificationExecutor<EmployeeClock> jpaSpecificationExecutor;

    final EmployeeServiceAdapter adapter;

    final MapStructMapper mapStructMapper = Mappers.getMapper(MapStructMapper.class);

    final EmployeeClockService employeeClockService = new EmployeeClockService();

    final HolidayService holidayService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/v1/oa/clock")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Operation(summary = "员工打卡")
    public void addClock() {
        val employeeId = this.adapter.currentLoginEmployeeId();
        EmployeeClock employeeClock = new EmployeeClock(employeeId);
        Optional<EmployeeClock> optional = this.repository.findOne(Example.of(employeeClock));
        employeeClock = optional.orElse(employeeClock);
        employeeClock.autoSign(9, 18);
        repository.save(employeeClock);
    }

    @GetMapping("/v1/oa/clock")
    @Operation(summary = "员工打卡详情")
    public EmployeeClockDTO getClock() {
        val employeeId = this.adapter.currentLoginEmployeeId();
        EmployeeClock employeeClock = new EmployeeClock(employeeId);
        Optional<EmployeeClock> optional = this.repository.findOne(Example.of(employeeClock));
        return mapStructMapper.mapper(optional.orElse(employeeClock));
    }

    @GetMapping("/v1/oa/clock/calendars")
    @Operation(summary = "员工工时日历")
    public Collection<EmployeeClockCalendar> getClockCalendars(@RequestParam(required = false) String month) {
        if (ObjectUtils.isEmpty(month)) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMM");
            month = fmt.format(new Date());
        }

        String id = this.adapter.currentLoginEmployeeId();
        List<EmployeeClock> clocks = this.jpaSpecificationExecutor.findAll(
                new MonthClockSpecification(id, month));

        Collection<MonthHolidayCalendar> holidays = this.holidayService.getCalender(month);

        return this.employeeClockService.build(clocks, holidays);
    }


}
