package cn.procsl.ping.boot.jpa.support.query.repository;

import cn.procsl.ping.boot.jpa.support.query.From;
import cn.procsl.ping.boot.jpa.support.query.Predicate;
import cn.procsl.ping.boot.jpa.support.query.ReferenceBy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
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

    public String sort() {
        return (this.path() == null || this.path().isEmpty() ? this.fieldName() : this.path());
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
        if (this.shouldIgnore()) {
            return Operator.NONE;
        }

        // 获取注解
        ReferenceBy reference = this.findAnnotations(ReferenceBy.class);

        // 获取hql表达式名称
        String sn = this.createSqlFieldFragment(from, reference);

        // 首先检测是否为 is null
        Parameter named = this.createNamedFragment(from, reference);
        Operator isNullOp = Operator.is_null(sn);
        Operator isNotNullOp = Operator.is_not_null(sn);

        return switch (this.predicate.operator()) {
            case ilike, between -> throw new UnsupportedOperationException("暂不支持");
            case eq -> createOperator(named, isNullOp, Operator.eq(sn, named.createPlaceholder()));
            case ne -> createOperator(named, isNotNullOp, Operator.ne(sn, named.createPlaceholder()));
            case gt -> createOperator(named, isNullOp, Operator.gt(sn, named.createPlaceholder()));
            case gte -> createOperator(named, isNullOp, Operator.gte(sn, named.createPlaceholder()));
            case lt -> createOperator(named, isNullOp, Operator.lt(sn, named.createPlaceholder()));
            case lte -> createOperator(named, isNullOp, Operator.lte(sn, named.createPlaceholder()));
            case like, left_like, right_like ->
                createOperator(named, isNullOp, Operator.like(sn, named.createPlaceholder()));
            case in -> createOperator(named, isNullOp, Operator.in(sn, named.createPlaceholder()));
            case not_in -> createOperator(named, isNotNullOp, Operator.not_in(sn, named.createPlaceholder()));
            case is_null -> (value instanceof Boolean && (Boolean) value) ? isNullOp : isNotNullOp;
            case is_not_null -> (value instanceof Boolean && (Boolean) value) ? isNotNullOp : isNullOp;
            case custom -> Operator.group(Operator.custom(named.createPlaceholder()));
        };
    }

    public <T extends Annotation> T findAnnotations(Class<T> annotation) {
        T anno = null;
        if (this.field != null) {
            anno = AnnotatedElementUtils.findMergedAnnotation(this.field, annotation);
        }
        if (anno != null) {
            return anno;
        }
        if (this.getter != null) {
            anno = AnnotatedElementUtils.findMergedAnnotation(this.getter, annotation);
        }
        return anno;
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

    private Parameter createNamedFragment(From from, ReferenceBy reference) {
        String alias = from.alias();
        String name = this.fieldName;
        if (reference != null && !reference.ref().isEmpty()) {
            alias = reference.ref();
        }
        if (reference != null && !reference.target().isEmpty()) {
            name = reference.target();
        }
        return new SimpleParameter(alias + "_" + toCamelCase(name), type, value);
    }

    public String toCamelCase(String str) {

        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(str.charAt(0)));

        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }

        return result.toString();
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
        boolean bool = this.path == null || this.path.isEmpty();
        if (!bool) {
            name = this.path;
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
