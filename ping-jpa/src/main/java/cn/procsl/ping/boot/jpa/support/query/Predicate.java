package cn.procsl.ping.boot.jpa.support.query;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @Predicate(path = {"name", "desc"}, logic = OR, op = like)
 * String keyword;
 * <p>
 * 构造如下
 * (name like '%keyword%' or desc like '%keyword%')
 * <p>
 * 可标注在类型或字段上
 */
@Documented
@Target({TYPE, FIELD})
@Retention(value = RUNTIME)
@Repeatable(value = Predicate.Predicates.class)
public @interface Predicate {
    /**
     * 只需要该字段值相同, 则为视为同一组, 同一组将会添加括号, 并且用OR连接
     * 不同组用and连接
     */
    String group() default "";

    /**
     * 当为null时是否忽略
     *
     * @return
     */
    boolean ignoreIfNull() default true;

    /**
     * 当为字符串时是否忽略该条件
     */
    boolean ignoreIfBlank() default true;

    /**
     * 如果是集合类型，是否在为空时忽略该查询
     */
    boolean ignoreIfEmptyCollection() default true;


    OperatorType operator();

    /**
     * 目标字段映射路径，支持嵌套路径，如 "entity.sub.name"
     * - 如果注解标注在字段上：默认使用字段名；
     * - 如果注解标注在类上：可以指定多个字段路径（多个字段复用同一条件值）
     */
    String[] path() default "";


    @Documented
    @Target({TYPE, FIELD})
    @Retention(value = RUNTIME)
    @interface Predicates {

        Predicate[] value();

    }

    /**
     * SqlOp：用于声明查询条件的操作符，用于 @Predicate 注解中。
     * 支持常见的 SQL 查询比较、模糊匹配、范围、空值判断等语义。
     */

    enum OperatorType {

        // ========================
        // 基本比较运算
        // ========================

        /**
         * 等于 (=)
         */
        eq,

        /**
         * 不等于 (!= 或 <>)
         */
        ne,

        /**
         * 大于 (>)
         */
        gt,

        /**
         * 大于等于 (>=)
         */
        gte,

        /**
         * 小于 (<)
         */
        lt,

        /**
         * 小于等于 (<=)
         */
        lte,

        // ========================
        // 模糊匹配
        // ========================

        /**
         * 模糊匹配，LIKE '%xxx%'
         */
        like,

        /**
         * 左匹配，LIKE 'xxx%'
         */
        left_like,

        /**
         * 右匹配，LIKE '%xxx'
         */
        right_like,

        /**
         * 不区分大小写的模糊匹配（如 PostgreSQL 的 ILIKE，或使用 lower(field) 实现）
         */
        ilike,

        // ========================
        // 多值匹配
        // ========================

        /**
         * IN (...)，包含于给定集合中
         */
        in,

        /**
         * NOT IN (...)，不包含于给定集合中
         */
        not_in,

        // ========================
        // 区间匹配
        // ========================

        /**
         * BETWEEN x AND y，范围查询，含边界
         */
        between,

        // ========================
        // 空值判断
        // ========================

        /**
         * IS NULL，字段为空
         */
        is_null,

        /**
         * IS NOT NULL，字段不为空
         */
        is_not_null,

        /**
         * 自定义表达式，允许你在 Predicate 注解中配合 expr 字段手写原生表达式（如 EXISTS 子查询）
         */
        custom
    }

}
