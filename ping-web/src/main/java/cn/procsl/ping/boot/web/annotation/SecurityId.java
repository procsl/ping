package cn.procsl.ping.boot.web.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE_USE, ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Schema(description = "主键ID", implementation = String.class, example = "Q0GgEiBbxEB4kCNHheTNb", minLength = 22, maxLength = 22)
@JacksonAnnotationsInside
@JacksonAnnotation
public @interface SecurityId {

    String scope();

}
