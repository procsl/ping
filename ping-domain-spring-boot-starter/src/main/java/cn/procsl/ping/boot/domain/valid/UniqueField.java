package cn.procsl.ping.boot.domain.valid;

import org.springframework.data.domain.Persistable;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 不要用于entity生命周期数据验证
 */
@Documented
@Constraint(validatedBy = {UniqueFieldValidator.class})
@Target({TYPE, FIELD, PARAMETER})
@Retention(RUNTIME)
@Repeatable(value = UniqueField.List.class)
public @interface UniqueField {

    String fieldName();

    String targetName() default "";

    Class<? extends Persistable<? extends Serializable>> entity();

    boolean useSpringEntityManager() default true;

    String message() default "{cn.procsl.ping.domain.valid.UniqueField.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({TYPE, FIELD, PARAMETER})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        UniqueField[] value();
    }
}
