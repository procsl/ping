package cn.procsl.ping.boot.web.encrypt;

import cn.procsl.ping.boot.common.utils.TokenCipher;
import cn.procsl.ping.boot.common.utils.TokenCipherWrapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class EncryptAndDecryptService implements EncryptDecryptService {

    final EncryptKeyService encryptKeyService;

    final EncryptContextService contextService;

    @Override
    public String encryptByContext(Long id, String scope) {
        String key = getKey();
        TokenCipher cipher = new TokenCipher(key, false, 128);
        TokenCipherWrapper wrapper = new TokenCipherWrapper(cipher, true);
        return wrapper.encrypt(id);
    }

    private String getKey() {
        EncryptContext context = contextService.getContext();
        return encryptKeyService.getKey(context);
    }

    @Override
    public Long decryptByContext(String source, String scope) throws DecryptException {
        String key = getKey();
        TokenCipher cipher = new TokenCipher(key, false, 128);
        TokenCipherWrapper wrapper = new TokenCipherWrapper(cipher, true);
        try {
            return wrapper.decryptToLong(source);
        } catch (Exception e) {
            throw new DecryptException(source, "解密失败", e);
        }
    }
}
