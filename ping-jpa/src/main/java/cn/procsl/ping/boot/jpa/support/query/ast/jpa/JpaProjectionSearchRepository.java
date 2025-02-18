package cn.procsl.ping.boot.jpa.support.query.ast.jpa;

import cn.procsl.ping.boot.jpa.support.query.*;
import cn.procsl.ping.boot.jpa.support.query.ast.Variable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@Indexed
@RequiredArgsConstructor
@Component("defaultJpaProjectionSearchRepository")
class JpaProjectionSearchRepository implements ProjectionSearchRepository {

    final EntityManager entityManager;

    @Override
    public <Q, R> List<R> search(Q query, Class<R> mapping) {

        ProjectionQueryExpression pqe = this.createProjectionQuery(query);

        String jpql = pqe.toExpString();

        Set<Variable> params = pqe.getQueryVariables();

        TypedQuery<R> eq = entityManager.createQuery(jpql, mapping);
        for (Variable param : params) {
            eq.setParameter(param.getName(), param.getValue());
        }
        log.info("sql语句: \n\n{}\n\n", jpql);

        return eq.getResultList();
    }

    private <Q> ProjectionQueryExpression createProjectionQuery(Q query) {
        Class<?> clazz = query.getClass();
        Projection projection = AnnotationUtils.findAnnotation(clazz, Projection.class);

        if (projection == null) {
            throw new IllegalStateException("未标注@Projection注解: " + clazz);
        }

        List<Join> joins = this.getJoinFields(clazz);
        List<Field> fields = ClassUtils.extractFields(clazz);

        ProjectionQueryExpression pqe = new ProjectionQueryExpression(true);
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            ReferenceBy ref = AnnotationUtils.findAnnotation(field, ReferenceBy.class);
            pqe.addSelect(new SelectFieldExpression(field, projection, ref));

            List<Where> wheres = this.getWheres(field);
            for (Where where : wheres) {
                pqe.addWhere(new WhereFieldExpression(query, field, projection, ref, where));
            }

            Order order = this.getOrder(field);
            if (order != null) {
                pqe.addOrder(new OrderFieldExpression(field, projection, ref, order, i));
            }
        }
        pqe.addFrom(new FromFieldExpression(projection, joins, clazz));
        return pqe;
    }

    private Order getOrder(Field field) {
        return AnnotationUtils.findAnnotation(field, Order.class);
    }

    private List<Where> getWheres(Field field) {
        List<Where> list = new ArrayList<>();

        Where.Wheres wheres = AnnotationUtils.findAnnotation(field, Where.Wheres.class);
        if (wheres != null) {
            list.addAll(Arrays.asList(wheres.value()));
        } else {
            Where where = AnnotationUtils.findAnnotation(field, Where.class);
            list.add(where);
        }
        return list;
    }

    private List<Join> getJoinFields(Class<?> clazz) {
        List<Join> list = new ArrayList<>();

        Join.Joins joins = AnnotationUtils.findAnnotation(clazz, Join.Joins.class);
        if (joins != null) {
            list.addAll(Arrays.asList(joins.value()));
        } else {
            Join join = AnnotationUtils.findAnnotation(clazz, Join.class);
            list.add(join);
        }
        return list;
    }


}
