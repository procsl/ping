package cn.procsl.ping.boot.common.web;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.annotation.*;

@Documented
@Operation
@ResponseBody
@VersionControl
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMethod.POST)
@ResponseStatus(HttpStatus.ACCEPTED)
public @interface Accepted {

    @AliasFor(annotation = RequestMapping.class, attribute = "path") String[] path();

    @AliasFor(annotation = RequestMapping.class, attribute = "params") String[] params() default {};

    @AliasFor(annotation = RequestMapping.class, attribute = "headers") String[] headers() default {};

    @AliasFor(annotation = RequestMapping.class, attribute = "consumes") String[] consumes() default {};

    @AliasFor(annotation = RequestMapping.class, attribute = "produces") String[] produces() default {};

    @AliasFor(annotation = Operation.class, attribute = "summary") String summary();

    @AliasFor(annotation = Operation.class, attribute = "description") String description() default "";

    @AliasFor(annotation = VersionControl.class, attribute = "version") String version() default "v1";

}
