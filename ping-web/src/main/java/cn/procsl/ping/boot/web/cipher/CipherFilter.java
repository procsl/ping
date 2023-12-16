package cn.procsl.ping.boot.web.cipher;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class CipherFilter extends OncePerRequestFilter {


    final Function<HttpServletRequest, Boolean> needDecryptRequest;

    final Function<HttpServletRequest, Boolean> needEncryptResponse;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {

        if (needDecryptRequest.apply(request)) {
            request = new HttpServletRequestDecryptWrapper(request);
        }

        boolean isEncrypt = needEncryptResponse.apply(request);
        if (isEncrypt) {
            response = new HttpServletResponseEncryptWrapper(request, response);
        }

        filterChain.doFilter(request, response);

        if (isEncrypt) {
            HttpServletResponseEncryptWrapper wrapper = (HttpServletResponseEncryptWrapper) response;
            wrapper.toEncrypt();
        }

    }

}
