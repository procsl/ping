package cn.procsl.ping.boot.oa.domain.salary;


import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
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

    @Column(length = 10, updatable = false, nullable = false)
    String employeeId;

    @Column(name = "salary_month", length = 7, updatable = false, nullable = false)
    String month;

    @Schema(description = "基础工资")
    @Column(precision = 12, scale = 2)
    BigDecimal basicWage;

    @Schema(description = "加班工资")
    @Column(precision = 12, scale = 2)
    BigDecimal overtimeWage;

    @Schema(description = "销售工资")
    @Column(precision = 12, scale = 2)
    BigDecimal salesMoneyWage;

    @Schema(description = "交通补助")
    @Column(precision = 12, scale = 2)
    BigDecimal trafficWage;

    @Schema(description = "考勤扣除")
    @Column(precision = 12, scale = 2)
    BigDecimal kaoQinReduce;

    @Schema(description = "保险扣除")
    @Column(precision = 12, scale = 2)
    BigDecimal secureReduce;

    @Schema(description = "税金扣除")
    @Column(precision = 12, scale = 2)
    BigDecimal taxReduce;

    @Builder
    EmployeeSalary(String employeeId, String month, BigDecimal basicWage, BigDecimal overtimeWage,
                   BigDecimal salesMoneyWage, BigDecimal trafficWage, BigDecimal kaoQinReduce,
                   BigDecimal secureReduce, BigDecimal taxReduce) {
        this.employeeId = employeeId;
        this.month = month;
        this.basicWage = basicWage;
        this.overtimeWage = overtimeWage;
        this.salesMoneyWage = salesMoneyWage;
        this.trafficWage = trafficWage;
        this.kaoQinReduce = kaoQinReduce;
        this.secureReduce = secureReduce;
        this.taxReduce = taxReduce;
    }

    public void editSalary(
            BigDecimal basicWage,
            BigDecimal overtimeWage,
            BigDecimal salesMoneyWage,
            BigDecimal trafficWage,
            BigDecimal kaoQinReduce,
            BigDecimal secureReduce,
            BigDecimal taxReduce
    ) {
        this.basicWage = basicWage;
        this.overtimeWage = overtimeWage;
        this.salesMoneyWage = salesMoneyWage;
        this.trafficWage = trafficWage;
        this.kaoQinReduce = kaoQinReduce;
        this.secureReduce = secureReduce;
        this.taxReduce = taxReduce;
    }

}
