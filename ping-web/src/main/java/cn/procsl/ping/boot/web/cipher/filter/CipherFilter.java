package cn.procsl.ping.boot.web.cipher.filter;

import cn.procsl.ping.boot.web.cipher.CipherLockupService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public final class CipherFilter extends OncePerRequestFilter {

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
        }

        boolean needEncrypt = requestResolver.needEncryptResponse(request, response);
        if (needEncrypt) {
            response = new HttpServletResponseEncryptWrapper(request, response);
        }

        boolean hasException = true;
        try {
            filterChain.doFilter(request, response);
            hasException = false;
        } finally {
            if (needEncrypt) {
                HttpServletResponseEncryptWrapper wrapper = (HttpServletResponseEncryptWrapper) response;
                wrapper.onResponseFinished(hasException);
            }

            if (request instanceof HttpServletRequestDecryptWrapper) {
                ((HttpServletRequestDecryptWrapper) request).onRequestFinished(hasException);
            }
        }


    }

}
