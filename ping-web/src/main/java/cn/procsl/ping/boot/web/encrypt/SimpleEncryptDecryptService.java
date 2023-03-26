package cn.procsl.ping.boot.web.encrypt;

import cn.procsl.ping.boot.common.utils.TokenCipher;
import cn.procsl.ping.boot.common.utils.TokenCipherWrapper;

public final class SimpleEncryptDecryptService implements EncryptDecryptService {

    String key = "123456789";

    @Override
    public String encryptByContext(Long id) {
        TokenCipher cipher = new TokenCipher(key, false, 128);
        TokenCipherWrapper wrapper = new TokenCipherWrapper(cipher, true);
        return wrapper.encrypt(id);
    }

    @Override
    public Long decryptByContext(String code) {
        TokenCipher cipher = new TokenCipher(key, false, 128);
        TokenCipherWrapper wrapper = new TokenCipherWrapper(cipher, true);
        return wrapper.decryptToLong(code);
    }
}
