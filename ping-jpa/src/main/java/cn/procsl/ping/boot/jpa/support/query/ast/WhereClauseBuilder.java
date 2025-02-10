package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.Projection;
import cn.procsl.ping.boot.jpa.support.query.ReferenceBy;
import cn.procsl.ping.boot.jpa.support.query.WhereField;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

final class WhereClauseBuilder implements QueryBuilder {

    @Override
    public Optional<List<? extends Clause>> parse(@NonNull QueryContext context) {

        Object instance = context.getQueryInstance();
        Class<?> query = context.getQueryClass();
        List<Field> fields = ClassUtils.extractFields(query);
        ArrayList<SimpleWhereClause> list = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);

            {
                WhereField.WhereFields whereFields = AnnotationUtils.findAnnotation(field, WhereField.WhereFields.class);
                if (whereFields != null) {
                    for (WhereField whereField : whereFields.value()) {
                        list.add(new SimpleWhereClause(instance, field, whereField, i));
                    }
                }
            }

            {
                WhereField whereField = AnnotationUtils.findAnnotation(field, WhereField.class);
                if (whereField == null) {
                    continue;
                }
                list.add(new SimpleWhereClause(instance, field, whereField, i));
            }
        }

        // 获取所有的field和getter
        return Optional.of(Collections.singletonList(new BuilderWhereClause(list, 0)));
    }

}

@RequiredArgsConstructor
final class BuilderWhereClause implements WhereClause {

    final List<SimpleWhereClause> where;
    @Getter
    final int order;

    @Override
    public String toClauseString() {
        Map<String, List<SimpleWhereClause>> gp = where.stream()
            .collect(Collectors.groupingBy(SimpleWhereClause::parmaGroup));
        ArrayList<String> ands = new ArrayList<>();
        gp.forEach((k, v) -> {
            v.sort(Comparator.comparingInt(Order::getOrder));
            String s = v.stream().filter(SimpleWhereClause::includeParma).map(Clause::toClauseString).collect(Collectors.joining(" or "));
            if (s.isEmpty()) {
                return;
            }
            StringBuilder we = new StringBuilder();
            if (v.size() > 1) {
                we.append('(').append(' ').append(s).append(' ').append(')');
            } else {
                we.append(s);
            }
            ands.add(we.toString());
        });
        return String.join(" and ", ands);
    }

}

@RequiredArgsConstructor
final class SimpleWhereClause implements WhereClause {

    final private Object instance;
    final private Field field;
    final private WhereField whereField;
    @Getter
    final private int order;
    private final Projection projection;
    private final ReferenceBy ref;

    public SimpleWhereClause(Object instance, Field field, WhereField whereField, int order) {
        this.instance = instance;
        this.field = field;
        this.whereField = whereField;
        this.order = order;
        this.ref = AnnotationUtils.findAnnotation(field, ReferenceBy.class);
        this.projection = AnnotationUtils.findAnnotation(field.getDeclaringClass(), Projection.class);
    }


    @SneakyThrows
    public Object getParamValue() {
        field.setAccessible(true);
        Object tmp = field.get(instance);
        field.setAccessible(false);
        return tmp;
    }

    public String parmaGroup() {
        String tmp = whereField.groupName();
        if (tmp == null || tmp.isEmpty()) {
            return this.getFieldParamName();
        }
        return tmp;
    }

    public boolean includeParma() {
        if (whereField.required()) {
            return true;
        }

        Object value = this.getParamValue();
        return value != null;
    }

    public boolean isRequired() {
        return whereField.required();
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

    public String getFieldParamName() {
        return field.getName();
    }

    @Override
    public String toClauseString() {
        String condition = this.whereField.condition();
        return "%s.%s %s :%s".formatted(this.getRefAlias(), this.getFieldName(), condition, this.getFieldParamName());
    }

}
