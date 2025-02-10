package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.Projection;
import cn.procsl.ping.boot.jpa.support.query.ReferenceBy;
import cn.procsl.ping.boot.jpa.support.query.SelectField;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
final class SelectClauseBuilder implements QueryBuilder {

    @Override
    public Optional<List<? extends Clause>> parse(@NonNull QueryContext context) {
        Class<?> query = context.getQueryClass();
        // 获取所有的field和getter
        List<Field> fields = ClassUtils.extractFields(query);
        ArrayList<SelectClause> list = new ArrayList<>();
        // 首先生成selects
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            SelectField fieldSelect = AnnotationUtils.findAnnotation(field, SelectField.class);
            if (fieldSelect == null) {
                continue;
            }
            list.add(new FieldSelectField(field, i));
        }
        return Optional.of(list);
    }

    @RequiredArgsConstructor
    private static class FieldSelectField implements SelectClause {

        final private Field field;
        @Getter
        final private int order;
        private final ReferenceBy ref;
        private final Projection projection;

        public FieldSelectField(Field field, int order) {
            this.field = field;
            this.order = order;
            this.ref = AnnotationUtils.findAnnotation(field, ReferenceBy.class);
            this.projection = AnnotationUtils.findAnnotation(field.getDeclaringClass(), Projection.class);
            if (this.ref == null && this.projection == null) {
                throw new IllegalArgumentException(field.getDeclaringClass() + "未标注 @Projection 注解");
            }
        }

        @Override
        public String toClauseString() {
            String tmp;
            if (ref == null) {
                tmp = this.projection.alias() + "." + this.field.getName();
                log.trace("[{}.{}]直接生成select字段[{}]", this.field.getDeclaringClass(), this.field.getName(), tmp);
                return tmp;
            }

            if (ref.alias() == null || ref.alias().isEmpty()) {
                tmp = ref.ref() + "." + ref.target() + " as " + this.field.getName();
            } else {
                tmp = ref.ref() + "." + ref.alias();
            }
            log.trace("[{}.{}]使用[ref]生成select字段[{}]", this.field.getDeclaringClass(), this.field.getName(), tmp);
            return tmp;
        }

        @Override
        public String getSelectFieldAlias() {
            return this.field.getName();
        }

        @Override
        public String getQueryColumnClause() {
            return null;
        }

        @Override
        public Class<?> getFieldMappingType() {
            return this.field.getType();
        }

    }


}
