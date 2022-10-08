package cn.procsl.ping.boot.oa.web.salary;

import cn.procsl.ping.boot.oa.web.SimpleEmployeeDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmployeeSalaryVO extends EmployeeSalaryDTO {

    Long id;

    SimpleEmployeeDTO employee;


    @Hidden
    @Schema(hidden = true)
    @JsonIgnore
    @Override
    public String getEmployeeId() {
        return super.getEmployeeId();
    }
}
