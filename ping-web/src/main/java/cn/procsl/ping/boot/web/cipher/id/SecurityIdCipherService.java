package cn.procsl.ping.boot.web.cipher.id;

import cn.procsl.ping.boot.common.utils.Base62;
import cn.procsl.ping.boot.common.utils.Scale62;
import cn.procsl.ping.boot.web.annotation.SecurityId;
import cn.procsl.ping.boot.web.cipher.CipherLockupService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RequiredArgsConstructor
final class SecurityIdCipherService {

    private final Base62 base62 = Base62.createInstance();

    private final CipherLockupService cipherLockupService;

    private final static CipherLockupService.CipherScope scope = CipherLockupService.CipherScope.session;

    public String encrypt(Long id, SecurityId securityId) throws IllegalBlockSizeException, BadPaddingException {
        if (securityId.scope().length() > 5) {
            throw new IllegalArgumentException("SecurityId scope 参数不应超过5个字符");
        }
        byte[] idBytes = Scale62.longToBytesBig(id);
        byte[] scopeBytes = securityId.scope().getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(idBytes.length + scopeBytes.length);
        buffer.put(idBytes);
        buffer.put(scopeBytes);

        Cipher cipher = cipherLockupService.lockupEncryptCipher(scope);
        byte[] enc = cipher.doFinal(buffer.array());
        cipherLockupService.release(scope, cipher);

        return base62.encodeToString(enc);
    }


    public Long decrypt(String source, SecurityId securityId) throws IllegalBlockSizeException, BadPaddingException {

        byte[] bytes = base62.decode(source.getBytes(StandardCharsets.UTF_8));

        Cipher cipher = cipherLockupService.lockupDecryptCipher(scope);
        byte[] res = cipher.doFinal(bytes);
        cipherLockupService.release(CipherLockupService.CipherScope.session, cipher);

        byte[] ss = Arrays.copyOfRange(res, 8, res.length);
        String scopeOrg = new String(ss);
        if (ObjectUtils.nullSafeEquals(securityId.scope(), scopeOrg)) {
            byte[] idArray = Arrays.copyOfRange(res, 0, 8);
            return Scale62.bytesToLongBig(idArray);
        }
        throw new IllegalArgumentException("ID错误");
    }
}
