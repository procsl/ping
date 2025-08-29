package cn.procsl.ping.boot.jpa.support.query.repository;

import cn.procsl.ping.boot.jpa.support.query.From;
import cn.procsl.ping.boot.jpa.support.query.Predicate;
import cn.procsl.ping.boot.jpa.support.query.ReferenceBy;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.temporal.Temporal;
import java.util.*;

record PredicateMetaRecord(Class<?> type, Object value,                // 实际字段值
                           String path, Predicate predicate, Source source, String fieldName, Field field, Method getter

) implements PredicateMeta {

    @Override
    public boolean isRoot() {
        return "$".equals(path);
    }

    @Override
    public Predicate getPredicate() {
        return this.predicate;
    }

    /**
     * 提取参数占位符,以及值
     * 一般是针对 path字段转换
     * 例如: a.b -> a_b
     * <p>
     * 对于值, 如果为like,会附加 %
     */
    @Override
    public List<Parameter> extractParameterPlaceholderAndValue() {
        return Collections.emptyList();
    }

    /**
     * 创建操作符
     */
    @Override
    public Operator createOperator(From from) {

        // 获取注解
        ReferenceBy reference = this.findAnnotations();

        TypeCategory c = this.categorize();
        // 获取hql表达式名称
        String sn = this.createSqlFieldFragment(from, reference);

        // 首先检测是否为 is null
        String named = this.createNamedFragment(from, reference, c);
        Operator isNullOp = Operator.is_null(sn);
        Operator isNotNullOp = Operator.is_not_null(sn);

        return switch (this.predicate.operator()) {
            case ilike, between -> throw new UnsupportedOperationException("暂不支持");
            case eq -> getOperator(named, isNullOp, Operator.eq(sn, named));
            case ne -> getOperator(named, isNotNullOp, Operator.ne(sn, named));
            case gt -> getOperator(named, isNullOp, Operator.gt(sn, named));
            case gte -> getOperator(named, isNullOp, Operator.gte(sn, named));
            case lt -> getOperator(named, isNullOp, Operator.lt(sn, named));
            case lte -> getOperator(named, isNullOp, Operator.lte(sn, named));
            case like, left_like, right_like -> getOperator(named, isNullOp, Operator.like(sn, named));
            case in -> getOperator(named, isNullOp, Operator.in(sn, named));
            case not_in -> getOperator(named, isNotNullOp, Operator.not_in(sn, named));
            case is_null -> (value instanceof Boolean && (Boolean) value) ? isNullOp : isNotNullOp;
            case is_not_null -> (value instanceof Boolean && (Boolean) value) ? isNotNullOp : isNullOp;
            case custom -> Operator.group(Operator.custom(named));
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

    private static Operator getOperator(String named, Operator operator, Operator sn) {
        if (named == null) {
            return operator;
        } else {
            return sn;
        }
    }


    private String createNamedFragment(From from, ReferenceBy reference, TypeCategory c) {
        return null;
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


    @Override
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

        if (this.ignoreIfEmptyCollection()) {
            return true;
        }

        return false;
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

    @Override
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
