package cn.procsl.ping.boot.oa.domain.clock;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@Schema
public class EmployeeClockCalendar implements Serializable {

    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm")
    Date startAt;
    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm")
    Date finishAt;
    @Schema(description = "工作日信息")
    @NotNull
    private MonthHolidayCalendar calendar;
    @Schema(description = "是否签到")
    @NotNull
    private Boolean clocked;

    public EmployeeClockCalendar(MonthHolidayCalendar info, EmployeeClock clock) {
        this.calendar = info;
        if (clock == null) {
            return;
        }
        this.clocked = clock.isClocked();
        this.finishAt = clock.getFinishAt();
        this.startAt = clock.getStartAt();
    }
}
