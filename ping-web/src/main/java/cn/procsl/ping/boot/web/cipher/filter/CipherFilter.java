package cn.procsl.ping.boot.web.cipher.filter;

import cn.procsl.ping.boot.web.cipher.CipherLockupService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public final class CipherFilter extends OncePerRequestFilter implements ServletRequestListener {

    final static String ON_REQUEST_DESTROYED = "ping.web.request.destroyed";
    final static String ON_RESPONSE_DESTROYED = "ping.web.response.destroyed";

    final CipherRequestResolver requestResolver;

    final CipherLockupService cipherLockupService;

    public CipherFilter(CipherRequestResolver requestResolver, CipherLockupService cipherLockupService) {
        this.cipherLockupService = cipherLockupService;
        if (requestResolver == null) {
            log.warn("未加载到加密解密解析器");
            requestResolver = new CipherRequestResolver() {
                @Override
                public boolean isEncryptionRequest(HttpServletRequest request, HttpServletResponse response) {
                    return false;
                }

                @Override
                public boolean needEncryptResponse(HttpServletRequest request, HttpServletResponse response) {
                    return false;
                }
            };
        }
        this.requestResolver = requestResolver;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {

        if (requestResolver.isEncryptionRequest(request, response)) {
            request = new HttpServletRequestDecryptWrapper(request, cipherLockupService);
            HttpServletRequestDecryptWrapper finalRequest = (HttpServletRequestDecryptWrapper) request;
            request.setAttribute(ON_REQUEST_DESTROYED, (Runnable) finalRequest::onRequestFinished);
        }

        boolean needEncrypt = requestResolver.needEncryptResponse(request, response);
        if (needEncrypt) {
            response = new HttpServletResponseEncryptWrapper(request, response, cipherLockupService);
            HttpServletResponseEncryptWrapper finalResponse = (HttpServletResponseEncryptWrapper) response;
            request.setAttribute(ON_RESPONSE_DESTROYED, (Runnable) finalResponse::onResponseFinished);
        }

        filterChain.doFilter(request, response);
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
}
