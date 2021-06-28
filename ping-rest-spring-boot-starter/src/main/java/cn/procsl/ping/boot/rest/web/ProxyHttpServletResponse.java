package cn.procsl.ping.boot.rest.web;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

/**
 * @author procsl
 * @date 2020/02/25
 */
public enum ProxyHttpServletResponse implements HttpServletResponse {

    INSTANCE;


    ProxyHttpServletResponse() {
    }

    @Override
    public void addCookie(Cookie cookie) {
        getResponse().addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return getResponse().containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        return getResponse().encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return getResponse().encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(String url) {
        return getResponse().encodeUrl(url);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return getResponse().encodeRedirectUrl(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        getResponse().sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        getResponse().sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        getResponse().sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        getResponse().setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        getResponse().addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        getResponse().setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        getResponse().addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        getResponse().setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        getResponse().addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc, String sm) {
        getResponse().setStatus(sc, sm);
    }

    @Override
    public int getStatus() {
        return getResponse().getStatus();
    }

    @Override
    public void setStatus(int sc) {
        getResponse().setStatus(sc);
    }

    @Override
    public String getHeader(String name) {
        return getResponse().getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return getResponse().getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return getResponse().getHeaderNames();
    }

    @Override
    public String getCharacterEncoding() {
        return getResponse().getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String charset) {
        getResponse().setCharacterEncoding(charset);
    }

    @Override
    public String getContentType() {
        return getResponse().getContentType();
    }

    @Override
    public void setContentType(String type) {
        getResponse().setContentType(type);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return getResponse().getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return getResponse().getWriter();
    }

    @Override
    public void setContentLength(int len) {
        getResponse().setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long length) {
        getResponse().setContentLengthLong(length);
    }

    @Override
    public int getBufferSize() {
        return getResponse().getBufferSize();
    }

    @Override
    public void setBufferSize(int size) {
        getResponse().setBufferSize(size);
    }

    @Override
    public void flushBuffer() throws IOException {
        getResponse().flushBuffer();
    }

    @Override
    public void resetBuffer() {
        getResponse().resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return getResponse().isCommitted();
    }

    @Override
    public void reset() {
        getResponse().reset();
    }

    @Override
    public Locale getLocale() {
        return getResponse().getLocale();
    }

    @Override
    public void setLocale(Locale loc) {
        getResponse().setLocale(loc);
    }

    protected HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }
}
