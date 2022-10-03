package cn.procsl.ping.boot.oa.domain.clock;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

@RequiredArgsConstructor
public class MonthClockSpecification implements Specification<EmployeeClock> {

    final String employeeId;
    final String clockMonth;

    @Override
    public Predicate toPredicate(@NonNull Root<EmployeeClock> root,
                                 @NonNull CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder cb) {
        Path<String> employeeIdField = root.get("employeeId");
        Path<String> clockDayField = root.get("clockDay");
        Predicate cd1 = cb.equal(employeeIdField, this.employeeId);
        Predicate cd2 = cb.like(clockDayField, clockMonth + "%");
        return cb.and(cd1, cd2);
    }

}
