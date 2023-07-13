package cn.procsl.ping.boot.web.cipher;

import cn.procsl.ping.boot.web.annotation.SecurityId;

/**
 * 可以使用对象池的技术缓存一些加密器解密器, 防止每个请求重新创建
 */
public interface CipherSecurityService {

    String encrypt(Long id, SecurityId scope);

    Long decrypt(String source, SecurityId scope) throws CipherException;

}
