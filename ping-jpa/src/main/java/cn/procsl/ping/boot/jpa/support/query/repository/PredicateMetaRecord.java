package cn.procsl.ping.boot.jpa.support.query.repository;

import cn.procsl.ping.boot.jpa.support.query.From;
import cn.procsl.ping.boot.jpa.support.query.Predicate;
import cn.procsl.ping.boot.jpa.support.query.ReferenceBy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.temporal.Temporal;
import java.util.*;

record PredicateMetaRecord(Class<?> type, Object value,                // 实际字段值
                           String path, Predicate predicate,
                           Source source, String fieldName,
                           Field field, Method getter

) {


    enum Source {
        type, method, field
    }

    enum TypeCategory {
        collection, simple, string, map, complex
    }

    public boolean isRoot() {
        return "$".equals(path);
    }


    /**
     * 提取参数占位符,以及值
     * 一般是针对 path字段转换
     * 例如: a.b -> a_b
     * <p>
     * 对于值, 如果为like,会附加 %
     */
    public List<Parameter> extractParameter(Operator operator) {
        if (operator instanceof ParameterOperator) {
            List<Parameter> tmp = ((ParameterOperator) operator).getNamed();
            return Objects.requireNonNullElse(tmp, Collections.emptyList());
        }
        return Collections.emptyList();
    }

    /**
     * 创建操作符
     */
    public Operator createOperator(From from) {

        // 获取注解
        ReferenceBy reference = this.findAnnotations();

        TypeCategory c = this.categorize();
        // 获取hql表达式名称
        String sn = this.createSqlFieldFragment(from, reference);

        // 首先检测是否为 is null
        Parameter named = this.createNamedFragment(from, reference, c, sn);
        Operator isNullOp = Operator.is_null(sn);
        Operator isNotNullOp = Operator.is_not_null(sn);

        return switch (this.predicate.operator()) {
            case ilike, between -> throw new UnsupportedOperationException("暂不支持");
            case eq -> createOperator(named, isNullOp, Operator.eq(sn, named.getName()));
            case ne -> createOperator(named, isNotNullOp, Operator.ne(sn, named.getName()));
            case gt -> createOperator(named, isNullOp, Operator.gt(sn, named.getName()));
            case gte -> createOperator(named, isNullOp, Operator.gte(sn, named.getName()));
            case lt -> createOperator(named, isNullOp, Operator.lt(sn, named.getName()));
            case lte -> createOperator(named, isNullOp, Operator.lte(sn, named.getName()));
            case like, left_like, right_like -> createOperator(named, isNullOp, Operator.like(sn, named.getName()));
            case in -> createOperator(named, isNullOp, Operator.in(sn, named.getName()));
            case not_in -> createOperator(named, isNotNullOp, Operator.not_in(sn, named.getName()));
            case is_null -> (value instanceof Boolean && (Boolean) value) ? isNullOp : isNotNullOp;
            case is_not_null -> (value instanceof Boolean && (Boolean) value) ? isNotNullOp : isNullOp;
            case custom -> Operator.group(Operator.custom(named.getName()));
        };
    }

    private ReferenceBy findAnnotations() {
        ReferenceBy reference = null;
        if (this.field != null) {
            reference = AnnotatedElementUtils.findMergedAnnotation(this.field, ReferenceBy.class);
        }
        if (reference != null) {
            return reference;
        }
        if (this.getter != null) {
            reference = AnnotatedElementUtils.findMergedAnnotation(this.getter, ReferenceBy.class);
        }
        if (reference != null) {
            return reference;
        }
        return null;
    }

    private static Operator createOperator(Parameter named, Operator operator, Operator sn) {
        if (named == null) {
            return operator;
        } else {
            return new ParameterOperator(Collections.singletonList(named), sn);
        }
    }

    @Getter
    @RequiredArgsConstructor
    final static class ParameterOperator implements Operator {

        final List<Parameter> named;
        final Operator operator;

        @Override
        public String toClauseString(BuilderContext context) {
            return operator.toClauseString(context);
        }

    }

    private Parameter createNamedFragment(From from, ReferenceBy reference, TypeCategory c, String templ) {
        String name = templ.replaceAll("\\.", "_");
        return new SimpleParameter(name, type, value);
    }


    public String createSqlFieldFragment(From from, ReferenceBy reference) {
        String alias = from.alias();
        String name = this.fieldName;
        if (reference != null && !reference.ref().isEmpty()) {
            alias = reference.ref();
        }
        if (reference != null && !reference.target().isEmpty()) {
            name = reference.target();
        }
        return "%s.%s".formatted(alias, name);
    }


    public boolean shouldIgnore() {

        if (predicate.operator() == Predicate.OperatorType.custom) {
            return false;
        }

        if (this.ignoreIfNull()) {
            return true;
        }

        if (this.ignoreIfBlank()) {
            return true;
        }

        return this.ignoreIfEmptyCollection();
    }

    private boolean ignoreIfEmptyCollection() {

        if (!this.predicate.ignoreIfEmptyCollection()) {
            return false;
        }

        if (type == null) {
            return false;
        }

        if (Collection.class.isAssignableFrom(type)) {
            if (value == null) {
                return true;
            }
            if (value instanceof Collection<?>) {
                if (((Collection<?>) value).isEmpty()) {
                    return true;
                }
            }
        }

        if (type.isArray()) {
            if (value == null) {
                return true;
            }
            return Array.getLength(value) == 0;
        }

        return false;
    }


    private boolean ignoreIfNull() {

        if (this.value != null) {
            return false;
        }

        if (!this.predicate.ignoreIfNull()) {
            return false;
        }

        if (type == null) {
            return false;
        }

        TypeCategory t = this.categorize();
        if (t != TypeCategory.simple && t != TypeCategory.complex) {
            return false;
        }

        return true;
    }

    public boolean ignoreIfBlank() {
        if (!this.predicate.ignoreIfBlank()) {
            return false;
        }
        if (type == null) {
            return false;
        }
        if (this.categorize() != TypeCategory.string) {
            return false;
        }
        if (this.value == null) {
            return true;
        }
        if (!(this.value instanceof String)) {
            return false;
        }
        return ((String) this.value).isBlank();
    }

    public TypeCategory categorize() {
        if (type == null) return null;

        if (type == String.class) return TypeCategory.string;

        if (type.isPrimitive() || Number.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type) || Date.class.isAssignableFrom(type) || Temporal.class.isAssignableFrom(type) || Enum.class.isAssignableFrom(type)) {
            return TypeCategory.simple;
        }

        if (type.isArray() || Collection.class.isAssignableFrom(type)) {
            return TypeCategory.collection;
        }

        if (Map.class.isAssignableFrom(type)) {
            return TypeCategory.map;
        }

        return TypeCategory.complex;
    }


}
