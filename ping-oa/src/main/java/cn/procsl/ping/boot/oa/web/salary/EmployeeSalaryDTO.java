package cn.procsl.ping.boot.oa.web.salary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class EmployeeSalaryDTO implements Serializable {

    String employeeId;

    @NotNull
    @NotEmpty
    String month;

    @Schema(description = "基础工资")
    @NotNull
    BigDecimal basicWage;

    @Schema(description = "加班工资")
    @NotNull
    BigDecimal overtimeWage;

    @Schema(description = "销售工资")
    @NotNull
    BigDecimal salesMoneyWage;

    @Schema(description = "交通补助")
    @NotNull
    BigDecimal trafficWage;

    @Schema(description = "考勤扣除")
    @NotNull
    BigDecimal kaoQinReduce;

    @Schema(description = "保险扣除")
    @NotNull
    BigDecimal secureReduce;

    @Schema(description = "税金扣除")
    @NotNull
    BigDecimal taxReduce;

}
