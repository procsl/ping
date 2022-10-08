package cn.procsl.ping.boot.oa.web.salary;

import cn.procsl.ping.boot.oa.domain.salary.EmployeeSalary;
import org.mapstruct.Mapper;

@Mapper
public interface SalaryMapper {

    EmployeeSalary mapper(EmployeeSalaryDTO dto);

}
