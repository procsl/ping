package cn.procsl.ping.boot.jpa.support.query;

import jakarta.persistence.criteria.JoinType;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({TYPE, FIELD})
@Retention(value = RUNTIME)
public @interface JoinOn {


    /**
     * 对应的实体字段,如果不填,默认为ID
     */
    String joinField() default "id";

    /**
     * 主表实体join字段, 如果不填默认为ID
     */
    String targetField() default "id";

    /**
     * 关联的实体
     */
    Class<?> joinEntity() default Void.class;

    /**
     * 连接类型
     */
    JoinType joinType() default JoinType.INNER;

}
