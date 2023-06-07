package cn.procsl.ping.boot.web.encrypt;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Schema(description = "ID", implementation = String.class, example = "Q0GgEiBbxEB4kCNHheTNb", minLength = 22, maxLength = 22)
@JacksonAnnotationsInside
@JsonSerialize(using = EncryptProcessor.class)
@JsonDeserialize(using = DecryptProcessor.class)
public @interface Encrypt {
}
