package cn.procsl.ping.boot.web.encrypt;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
final public class DecryptConversionService implements Converter<String, DecryptDTO> {

    final EncryptDecryptService decryptService;

    @Override
    public DecryptDTO convert(String source) {
        Long res = decryptService.decryptByContext(source);
        return new DecryptDTO(res);
    }


}
