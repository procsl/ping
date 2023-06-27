package cn.procsl.ping.boot.web.annotation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE_USE, ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@RequestBody(content =
@Content(array = @ArraySchema(
        schema = @Schema(
                implementation = String.class,
                example = "Q0GgEiBbxEB4kCNHheTNb",
                minLength = 22, maxLength = 22),
        uniqueItems = true)
))
public @interface SecurityIds {
}
