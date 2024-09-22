package cn.procsl.ping.boot.jpa.support.query;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(value = RUNTIME)
@Target(TYPE)
public @interface QueryBind {

    /**
     * 基本查询语句骨架: 无select, where, order by
     * 不支持 group by
     */
    String base();

}
