package cn.procsl.ping.boot.web.cipher;

import cn.procsl.ping.boot.common.utils.Base62;
import cn.procsl.ping.boot.common.utils.Scale62;
import cn.procsl.ping.boot.common.utils.TokenCipher;
import cn.procsl.ping.boot.web.annotation.SecurityId;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RequiredArgsConstructor
public final class SimplerCipherSecurityService implements CipherSecurityService {

    final Base62 base62 = Base62.createInstance();
    final String key = "12345678";

    @Override
    public String encrypt(Long id, SecurityId scope) {
        TokenCipher cipher = new TokenCipher(key, false, 128);

        byte[] idBytes = Scale62.longToBytesBig(id);
        byte[] scopeBytes = scope.scope().getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(idBytes.length + scopeBytes.length);
        buffer.put(idBytes);
        buffer.put(scopeBytes);
        byte[] enc = cipher.encrypt(buffer.array());
        return base62.encodeToString(enc);
    }


    @Override
    public Long decrypt(String source, SecurityId scope) throws CipherException {
        TokenCipher cipher = new TokenCipher(key, false, 128);

        byte[] bytes = base62.decode(source.getBytes(StandardCharsets.UTF_8));
        byte[] res = cipher.decrypt(bytes);

        byte[] ss = Arrays.copyOfRange(res, 8, res.length);
        String scopeOrg = new String(ss);
        if (ObjectUtils.nullSafeEquals(scope.scope(), scopeOrg)) {
            byte[] idArray = Arrays.copyOfRange(res, 0, 8);
            return Scale62.bytesToLongBig(idArray);
        }
        throw new IllegalArgumentException("scope 错误");
    }
}
