package cn.procsl.ping.boot.web.encrypt;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.Converter;

@Slf4j
@RequiredArgsConstructor
final public class DecryptConversionService implements Converter<String, Long>, ConditionalConverter {

    final EncryptDecryptService decryptService;

    @Override
    public Long convert(@Nonnull String source) {
//        Long res = decryptService.decryptByContext(source);
        log.info("test");
        return 1L;
    }


    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return targetType.hasAnnotation(SecurityId.class);
    }
}