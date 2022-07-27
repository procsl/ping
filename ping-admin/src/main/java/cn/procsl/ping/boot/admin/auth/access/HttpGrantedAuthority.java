package cn.procsl.ping.boot.admin.auth.access;

import cn.procsl.ping.boot.admin.domain.rbac.HttpPermission;
import cn.procsl.ping.boot.common.utils.ObjectUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

@Slf4j
@Getter
@EqualsAndHashCode
public class HttpGrantedAuthority implements GrantedAuthority {

    final static PathPatternParser parser = PathPatternParser.defaultInstance;

    final static Pattern DYNAMIC_PATTERN = compile("[*{}?]+");

    @Hidden
    @JsonIgnore
    final String method;

    @Hidden
    @JsonIgnore
    final String uri;

    @Transient
    @Hidden
    @JsonIgnore
    private Boolean dynamic;

    @Transient
    @Hidden
    @JsonIgnore
    private PathPattern matcher;

    public HttpGrantedAuthority(String method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public HttpGrantedAuthority(HttpPermission httpPermission) {
        this.method = httpPermission.getHttpMethod();
        this.uri = httpPermission.getUrl();
    }

    @Override
    public String getAuthority() {
        return String.format("%s:%s", method, uri);
    }


    public boolean matcher(HttpServletRequest request) {
        if (!ObjectUtils.nullSafeEquals(request.getMethod(), this.method)) {
            return false;
        }

        String url = request.getRequestURI();
        if (ObjectUtils.nullSafeEquals(url, this.uri)) {
            this.log(url);
            return true;
        }

        if (this.dynamic == null) {
            dynamic = DYNAMIC_PATTERN.matcher(this.uri).find();
        }

        if (!dynamic) {
            return false;
        }

        if (this.matcher == null) {
            this.matcher = parser.parse(this.uri);
        }

        PathContainer container = PathContainer.parsePath(url, PathContainer.Options.HTTP_PATH);
        if (this.matcher.matches(container)) {
            this.log(url);
            return true;
        }
        return false;
    }

    void log(String url) {
        log.debug("匹配权限:{}, URL:{}", this.getAuthority(), url);
    }

    @Override
    public String toString() {
        return getAuthority();
    }
}
