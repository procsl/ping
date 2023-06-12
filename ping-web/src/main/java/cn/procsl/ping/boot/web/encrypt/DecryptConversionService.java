package cn.procsl.ping.boot.web.encrypt;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNullApi;

@RequiredArgsConstructor
final public class DecryptConversionService implements Converter<String, DecryptDTO> {

    final EncryptDecryptService decryptService;

    @Override
    public DecryptDTO convert(@Nonnull String source) {
        Long res = decryptService.decryptByContext(source);
        return new DecryptDTO(res);
    }


}
