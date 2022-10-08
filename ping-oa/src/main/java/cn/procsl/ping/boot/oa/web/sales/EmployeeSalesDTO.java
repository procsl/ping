package cn.procsl.ping.boot.oa.web.sales;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class EmployeeSalesDTO implements Serializable {

    @NotNull
    @NotEmpty
    String employeeId;

    @Schema(description = "销售月份")
    @NotNull
    @NotEmpty
    String month;

    @Schema(description = "销售金额")
    BigDecimal money;

}
