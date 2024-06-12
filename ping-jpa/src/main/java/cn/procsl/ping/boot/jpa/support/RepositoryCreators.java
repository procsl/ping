package cn.procsl.ping.boot.jpa.support;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(TYPE)
@Retention(value = RUNTIME)
public @interface RepositoryCreators {

    RepositoryCreator[] value();

}
