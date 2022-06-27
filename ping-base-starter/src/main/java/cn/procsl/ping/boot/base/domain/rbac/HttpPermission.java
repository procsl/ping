package cn.procsl.ping.boot.base.domain.rbac;

import lombok.*;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@Entity
@AllArgsConstructor
@DiscriminatorValue("http")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HttpPermission extends Permission {

    final static java.util.regex.Pattern dynamic = java.util.regex.Pattern.compile("[*{}?]+");

    @Pattern(regexp = "(GET|POST|DELETE|PATCH|PUT)", message = "仅支持[{regexp}]方法") @NotBlank String operate;

    @NotBlank String resource;

    public static Permission create(@NonNull String httpMethod, @NonNull String url) {
        return new HttpPermission(httpMethod.toUpperCase(), url);
    }

    @Transient
    public String getHttpMethod() {
        return this.getOperate();
    }

    @Transient
    public String getUrl() {
        return this.getResource();
    }

    public boolean isDynamicURI() {
        return dynamic.matcher(resource).matches();
    }
}
