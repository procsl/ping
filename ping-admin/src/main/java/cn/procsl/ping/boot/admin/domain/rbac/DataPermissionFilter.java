package cn.procsl.ping.boot.admin.domain.rbac;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.METHOD})
@Retention(RUNTIME)
public @interface DataPermissionFilter {

    String filter();

    String key();

}
