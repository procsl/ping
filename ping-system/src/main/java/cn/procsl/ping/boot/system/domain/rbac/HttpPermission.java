package cn.procsl.ping.boot.system.domain.rbac;

import lombok.*;
import org.springframework.http.server.PathContainer;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static java.util.regex.Pattern.compile;
import static org.springframework.http.server.PathContainer.parsePath;

@Getter
@Setter
@Entity
@DiscriminatorValue("http")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HttpPermission extends Permission {


    @Id
    @GeneratedValue
    Long id;

    @NotBlank
    @Column(updatable = false)
    @Pattern(regexp = "(GET|POST|DELETE|PATCH|PUT)", message = "仅支持[{regexp}]方法") String operate;

    @NotBlank
    @Column(updatable = false)
    String resource;
    final static PathPatternParser parser = PathPatternParser.defaultInstance;
    private final static java.util.regex.Pattern DYNAMIC_PATTERN = compile("[*{}?]+");
    @Transient
    private Boolean dynamic;
    @Transient
    private PathPattern matcher;

    public static HttpPermission create(@NonNull String httpMethod, @NonNull String url) {
        return new HttpPermission(httpMethod.toUpperCase(), url);
    }

    HttpPermission(String operate, String resource) {
        this.operate = operate;
        this.resource = resource;
    }

    @Transient
    public String getHttpMethod() {
        return this.getOperate();
    }

    @Transient
    public String getUrl() {
        return this.getResource();
    }

    public boolean matcher(String httpMethod, String url) {
        if (!ObjectUtils.nullSafeEquals(httpMethod, this.getHttpMethod())) {
            return false;
        }

        if (ObjectUtils.nullSafeEquals(url, this.getUrl())) {
            return true;
        }

        if (this.dynamic == null) {
            dynamic = DYNAMIC_PATTERN.matcher(this.getUrl()).find();
        }

        if (!dynamic) {
            return false;
        }

        if (this.matcher == null) {
            this.matcher = parser.parse(this.getUrl());
        }

        PathContainer container = parsePath(url, PathContainer.Options.HTTP_PATH);
        return this.matcher.matches(container);
    }

}
