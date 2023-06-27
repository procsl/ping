package cn.procsl.ping.boot.web.annotation;

import cn.procsl.ping.boot.web.encrypt.DecryptProcessor;
import cn.procsl.ping.boot.web.encrypt.EncryptProcessor;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE_USE, ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Schema(description = "主键ID", implementation = String.class, example = "Q0GgEiBbxEB4kCNHheTNb", minLength = 22, maxLength = 22)
@JacksonAnnotationsInside
@JsonSerialize(using = EncryptProcessor.class, contentUsing = EncryptProcessor.class, contentAs = Long.class, as = Long.class)
@JsonDeserialize(using = DecryptProcessor.class, contentUsing = DecryptProcessor.class, contentAs = Long.class, as = Long.class)
public @interface SecurityId {


}
