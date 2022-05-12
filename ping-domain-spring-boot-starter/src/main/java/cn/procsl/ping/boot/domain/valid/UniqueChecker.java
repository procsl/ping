package cn.procsl.ping.boot.domain.valid;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.Persistable;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.ConstraintViolationException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class UniqueChecker implements UniqueService {

    private final EntityManager entityManager;

    public Optional<?> query(Class<? extends Persistable<?>> entity, String uniqueFieldName, Object uniqueData) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        Method id = ReflectionUtils.findMethod(entity, "getId");
        assert id != null;
        val criteriaQuery = builder.createQuery(id.getReturnType());

        val root = criteriaQuery.from(entity);
        criteriaQuery.select(root.get("id"));
        criteriaQuery.where(builder.equal(root.get(uniqueFieldName), uniqueData));
        // 这里需要以无事务的方式运行
        try {
            List<?> ids = entityManager.createQuery(criteriaQuery).setMaxResults(1).getResultList();
            if (ids.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(ids.get(0));
        } catch (Exception e) {
            log.error("查询数据库出现错误:", e);
            throw e;
        }
    }

    @Override
    public void valid(@NonNull Class<?> entity) throws ConstraintViolationException {
//        if (entity.isAssignableFrom(Persistable.class)) {
//            throw new IllegalArgumentException(entity.getName() + "is not a entity");
//        }
//

//        Optional<?> dbid = this.query((Class<? extends Persistable<?>>) entity, uniqueFieldName, uniqueData);
//        if (ObjectUtils.isEmpty(id)) {
//            return;
//        }
//        if (ObjectUtils.nullSafeEquals(dbid, id)) {
//            return;
//        }
//        throw new ConstraintViolationException(message, null);
    }

}
