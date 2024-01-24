package cn.procsl.ping.boot.web.cipher.id;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import cn.procsl.ping.boot.web.cipher.CipherException;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Collections;
import java.util.Set;

@RequiredArgsConstructor
final class CipherGenericConverter implements GenericConverter, ConditionalGenericConverter {

    final SecurityIdCipherService cipherLockupService;


    @Override
    public boolean matches(@Nonnull TypeDescriptor sourceType, TypeDescriptor targetType) {
        return targetType.hasAnnotation(SecurityId.class);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Long.class));
    }

    @Override
    public Object convert(Object source, @Nonnull TypeDescriptor sourceType, TypeDescriptor targetType) {
        SecurityId security = targetType.getAnnotation(SecurityId.class);
        if (security == null) {
            throw new IllegalArgumentException("找不到指定的注解");
        }
        try {
            return cipherLockupService.decrypt((String) source, security);
        } catch (Exception e) {
            throw new ConverterException("id", (String) source, e.getMessage(), e);
        }
    }

    @Getter
    static class ConverterException extends CipherException {

        final String filedName;
        final String source;

        public ConverterException(String fieldName, String source, String message, Throwable e) {
            super(message, e);
            this.filedName = fieldName;
            this.source = source;
        }
    }

    static class ErrorProcessWevConversionService extends WebConversionService {

        public ErrorProcessWevConversionService() {
            super(new DateTimeFormatters());
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, @Nonnull TypeDescriptor targetType) {
            try {
                return super.convert(source, sourceType, targetType);
            } catch (ConversionFailedException e) {
                Throwable cause = e.getCause();
                if (cause instanceof ConverterException converterException) {
                    throw converterException;
                }
                throw e;
            }
        }
    }

}
