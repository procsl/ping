package cn.procsl.ping.boot.web.encrypt;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Collections;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
final public class DecryptConversionService implements GenericConverter, ConditionalGenericConverter {

    final EncryptDecryptService decryptService;


    @Override
    public boolean matches(@Nonnull TypeDescriptor sourceType, TypeDescriptor targetType) {
        return targetType.hasAnnotation(SecurityId.class);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Long.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        SecurityId security = targetType.getAnnotation(SecurityId.class);
        if (security == null) {
            throw new IllegalArgumentException("找不到指定的注解");
        }
        return decryptService.decryptByContext((String) source, security.scope());
    }


}

