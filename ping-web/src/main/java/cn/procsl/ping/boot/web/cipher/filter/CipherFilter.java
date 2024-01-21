package cn.procsl.ping.boot.web.cipher.filter;

import cn.procsl.ping.boot.web.annotation.Encryption;
import cn.procsl.ping.boot.web.cipher.CipherException;
import cn.procsl.ping.boot.web.cipher.CipherLockupService;
import cn.procsl.ping.boot.web.component.MethodAnnotationInterceptor;
import jakarta.annotation.Nonnull;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MimeType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;

import java.io.IOException;

import static cn.procsl.ping.boot.web.cipher.filter.CipherEncodeType.ENCRYPT_MIME_TYPE;
import static cn.procsl.ping.boot.web.cipher.filter.CipherEncodeType.ENCRYPT_MIME_TYPE_VALUE;

@Slf4j
public final class CipherFilter extends OncePerRequestFilter implements ServletRequestListener,
        MethodAnnotationInterceptor<Encryption> {

    final static String ON_REQUEST_DESTROYED = "cn.ping.web.request.destroyed";

    final static String ON_RESPONSE_DESTROYED = "cn.ping.web.response.destroyed";

    final CipherLockupService cipherLockupService;

    public CipherFilter(CipherLockupService cipherLockupService) {
        this.cipherLockupService = cipherLockupService;
    }

    MimeType mimeResolver(HttpServletRequest request, HttpServletResponse response) {
        String contentType = request.getContentType();
        if (!contentType.startsWith(ENCRYPT_MIME_TYPE_VALUE)) {
            return null;
        }
        try {
            MimeType mime = MimeType.valueOf(contentType);
            if (ENCRYPT_MIME_TYPE.equalsTypeAndSubtype(mime)) {
                return mime;
            }
        } catch (RuntimeException ignored) {
        }
        return null;
    }


    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {

        MimeType mime = this.mimeResolver(request, response);
        if (mime == null) {
            filterChain.doFilter(request, response);
            return;
        }
        HttpServletRequestDecryptWrapper finalRequest = new HttpServletRequestDecryptWrapper(request,
                cipherLockupService, mime);
        request.setAttribute(ON_REQUEST_DESTROYED, (Runnable) finalRequest::onRequestFinished);

        HttpServletResponseEncryptWrapper finalResponse = new HttpServletResponseEncryptWrapper(request,
                response, cipherLockupService, mime);
        request.setAttribute(ON_RESPONSE_DESTROYED, (Runnable) finalResponse::onResponseFinished);

        filterChain.doFilter(finalRequest, finalResponse);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        toDestroy(sre, ON_REQUEST_DESTROYED);
        toDestroy(sre, ON_RESPONSE_DESTROYED);
    }

    private void toDestroy(ServletRequestEvent sre, String key) {
        ServletRequest req = sre.getServletRequest();
        Object onDestroy = req.getAttribute(key);
        if (onDestroy == null) {
            return;
        }
        Runnable dest = (Runnable) (onDestroy);
        try {
            dest.run();
        } catch (Exception e) {
            log.warn("回收时失败", e);
        }
    }

    @Override
    public Class<Encryption> getAnnotationClass() {
        return Encryption.class;
    }

    @Override
    public boolean doPreHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler, Encryption annotation) {
        if (annotation == null) {
            return true;
        }

        boolean requestCheck = (request instanceof HttpServletRequestDecryptWrapper || request.getAttribute(ON_REQUEST_DESTROYED) != null);
        if (!requestCheck) {
            throw new CipherException("请求解密失败,缺少必要参数", null);
        }
        return false;
    }

}
