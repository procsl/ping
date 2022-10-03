package cn.procsl.ping.boot.oa.web.clock;

import cn.procsl.ping.boot.oa.domain.clock.EmployeeClock;
import org.mapstruct.Mapper;


@Mapper
public interface MapStructMapper {

    EmployeeClockDTO mapper(EmployeeClock employeeClock);

}
