package cn.procsl.ping.boot.oa.domain.salary;


import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@ToString
@RepositoryCreator
@Table(name = "o_employee_salary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmployeeSalary extends AbstractPersistable<Long> implements Serializable {

    String employeeId;

    String month;

    @Schema(description = "基础工资")
    BigDecimal basicWage;

    @Schema(description = "加班工资")
    BigDecimal overtimeWage;

    @Schema(description = "销售工资")
    BigDecimal seilmoneyWage;

    @Schema(description = "交通补助")
    BigDecimal trafficWage;

    @Schema(description = "考勤扣除")
    BigDecimal kaoqinReduce;

    @Schema(description = "保险扣除")
    BigDecimal secureReduce;

    @Schema(description = "税金扣除")
    BigDecimal taxReduce;

}
