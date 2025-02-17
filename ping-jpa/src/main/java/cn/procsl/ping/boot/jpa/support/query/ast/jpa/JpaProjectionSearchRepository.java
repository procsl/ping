package cn.procsl.ping.boot.jpa.support.query.ast.jpa;

import cn.procsl.ping.boot.jpa.support.query.Join;
import cn.procsl.ping.boot.jpa.support.query.Projection;
import cn.procsl.ping.boot.jpa.support.query.ProjectionSearchRepository;
import cn.procsl.ping.boot.jpa.support.query.Where;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Indexed
@Component("defaultJpaProjectionSearchRepository")
class JpaProjectionSearchRepository implements ProjectionSearchRepository {

    @Override
    public <Q, R> List<R> search(Q query, Class<R> mapping) {

        Class<?> clazz = query.getClass();
        Projection projection = AnnotationUtils.findAnnotation(clazz, Projection.class);

        if (projection == null) {
            throw new IllegalStateException("未标注@Projection注解: " + clazz);
        }

        List<Join> joins = this.getJoinFields(clazz);
        List<Field> fields = ClassUtils.extractFields(clazz);

        ProjectionQueryExpression pqe = new ProjectionQueryExpression(true);
        for (Field field : fields) {
            pqe.addSelect(new SelectFieldExpression(field, projection));

            List<Where> wheres = this.getWheres(field);
            for (Where where : wheres) {
                pqe.addWhere(new WhereFieldExpression(field, where));
            }

        }
        pqe.addFrom(new FromFieldExpression(projection, joins, clazz));

        log.info("sql语句: \n\n{}\n\n", pqe.toExpString());

        return null;
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
