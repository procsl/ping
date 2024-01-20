package cn.procsl.ping.boot.web.alias;

import cn.procsl.ping.boot.web.cipher.CipherLockupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Enumeration;
import java.util.Map;

public class AliasHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final CipherLockupService cipherLockupService;
    private String requestURI;

    public AliasHttpServletRequestWrapper(HttpServletRequest request, CipherLockupService cipherLockupService) {
        super(request);
        this.cipherLockupService = cipherLockupService;
    }

    @Override
    public String getRequestURI() {
        String tmp = super.getRequestURI();
        return null;
    }

    @Override
    public String getParameter(String name) {
        return super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return super.getParameterMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return super.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return super.getParameterValues(name);
    }

    @Override
    public StringBuffer getRequestURL() {
        return super.getRequestURL();
    }

}
