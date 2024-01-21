package cn.procsl.ping.boot.web.cipher;

import javax.crypto.Cipher;

/**
 * 可以使用对象池的技术缓存一些加密器解密器, 防止每个请求重新创建
 */
public interface CipherLockupService {

    enum CipherScope {
        application, session, request
    }

    Cipher lockupEncryptCipher(CipherScope scope);

    Cipher lockupDecryptCipher(CipherScope scope);

    void release(CipherScope scope, Cipher cipher);

}
