package cn.procsl.ping.boot.system.domain.secret;

import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

/**
 * 获取可用私钥
 */
@NoArgsConstructor
@AllArgsConstructor
public class PrivateKeySpec implements Specification<PrivateKey> {

    Long id;


    @Override
    public Predicate toPredicate(Root<PrivateKey> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Path<Date> forceDate = root.get("forceDate");
        Path<Date> expiredDate = root.get("expiredDate");
        Path<Long> idExp = root.get("id");

        Date now = new Date();

        // 小于等于生效时间
        Predicate where = cb.lessThanOrEqualTo(forceDate, now);

        // 大于等于失效时间
        where = cb.and(where, cb.greaterThanOrEqualTo(expiredDate, now));

        // 大于指定的ID
        if (id != null) {
            Predicate tmp = cb.greaterThanOrEqualTo(idExp, id);
            where = cb.and(where, tmp);
        }

        // 按照ID排序
        Order desc = cb.desc(idExp);

        return query.where(where).orderBy(desc).getRestriction();

    }

}
