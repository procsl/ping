package cn.procsl.ping.boot.web.cipher.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface CipherRequestResolver {

    boolean isEncryptionRequest(HttpServletRequest request, HttpServletResponse response);

    boolean needEncryptResponse(HttpServletRequest request, HttpServletResponse response);

}
