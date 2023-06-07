package cn.procsl.ping.boot.web.component;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public final class HttpMethodRequestWrapper extends HttpServletRequestWrapper {

    final String method;

    public HttpMethodRequestWrapper(HttpServletRequest request, String method) {
        super(request);
        this.method = method;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

}
