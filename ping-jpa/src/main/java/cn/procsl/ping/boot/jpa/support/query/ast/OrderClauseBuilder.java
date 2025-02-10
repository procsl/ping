package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.OrderByField;
import cn.procsl.ping.boot.jpa.support.query.Projection;
import cn.procsl.ping.boot.jpa.support.query.ReferenceBy;
import lombok.NonNull;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderClauseBuilder implements QueryBuilder {
    @Override
    public Optional<List<? extends Clause>> parse(@NonNull QueryContext context) {

        Class<?> query = context.getQueryClass();
        ArrayList<SimpleOrderClause> list = new ArrayList<>();
        List<Field> fields = ClassUtils.extractFields(query);
        // 首先生成selects
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            OrderByField orderBy = AnnotationUtils.findAnnotation(field, OrderByField.class);
            if (orderBy == null) {
                continue;
            }
            list.add(new SimpleOrderClause(i, orderBy, field));
        }
        return Optional.of(list);
    }

    private static class SimpleOrderClause implements OrderByClause {

        final int order;

        final OrderByField orderByField;
        final Field field;
        private final ReferenceBy ref;
        private final Projection projection;

        public SimpleOrderClause(int order, OrderByField orderByField, Field field) {
            this.order = order;
            this.orderByField = orderByField;
            this.field = field;
            this.ref = AnnotationUtils.findAnnotation(field, ReferenceBy.class);
            this.projection = AnnotationUtils.findAnnotation(field.getDeclaringClass(), Projection.class);
        }

        /**
         * 引用别名
         */
        public String getRefAlias() {
            if (ref != null) {
                return ref.ref();
            }
            if (projection == null) {
                Class<?> main = field.getDeclaringClass();
                throw new IllegalArgumentException("未标注Projection: " + main);
            }
            return projection.alias();
        }

        public String getFieldName() {
            if (ref == null) {
                return field.getName();
            }
            if (ref.target() == null || ref.target().isEmpty()) {
                return field.getName();
            }
            return ref.target();
        }

        /**
         * order by xx.xx
         */
        @Override
        public String toClauseString() {
            return "%s.%s %s".formatted(this.getRefAlias(), this.getFieldName(), this.getDirection());
        }

        @Override
        public String getDirection() {
            return orderByField.sort().name();
        }

        public int getOrder() {
            if (this.orderByField.order() == Integer.MAX_VALUE) {
                return order + 100000;
            }
            return this.orderByField.order();
        }
    }

}
