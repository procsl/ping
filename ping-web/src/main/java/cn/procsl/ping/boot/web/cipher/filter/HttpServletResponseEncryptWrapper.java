package cn.procsl.ping.boot.web.cipher.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.util.Collection;

final class HttpServletResponseEncryptWrapper extends HttpServletResponseWrapper {


    public HttpServletResponseEncryptWrapper(HttpServletRequest request, HttpServletResponse response) {
        super(response);
    }


    @Override
    public Collection<String> getHeaders(String name) {
        return super.getHeaders(name);
    }

    void onResponseFinished(boolean hasException) {
    }


}
