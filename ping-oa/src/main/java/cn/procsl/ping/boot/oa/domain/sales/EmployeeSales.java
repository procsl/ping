package cn.procsl.ping.boot.oa.domain.sales;

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
@Table(name = "o_employee_sales")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmployeeSales extends AbstractPersistable<Long> implements Serializable {

    @Column(length = 10, updatable = false, nullable = false)
    String employeeId;

    @Schema(description = "销售月份")
    @Column(length = 8, name = "sales_month", updatable = false, nullable = false)
    String month;

    @Schema(description = "销售金额")
    @Column(precision = 12, scale = 2)
    BigDecimal money;

    public EmployeeSales(String employeeId, String month, BigDecimal money) {
        this.employeeId = employeeId;
        this.month = month;
        this.money = money;
    }

    public void changeSales(BigDecimal money) {
        this.money = money;
    }
}
