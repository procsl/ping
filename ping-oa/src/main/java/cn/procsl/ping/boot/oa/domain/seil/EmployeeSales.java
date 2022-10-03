package cn.procsl.ping.boot.oa.domain.seil;

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
@Table(name = "o_employee_sales")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmployeeSales extends AbstractPersistable<Long> implements Serializable {

    String employeeId;

    @Schema(description = "销售月份")
    String month;

    @Schema(description = "销售金额")
    BigDecimal money;

}
