package cn.procsl.ping.boot.web.cipher;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Collections;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
final public class CipherGenericConverter implements GenericConverter, ConditionalGenericConverter {

    final CipherSecurityService cipherSecurityService;


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
            return cipherSecurityService.decrypt((String) source, security);
        } catch (Exception e) {
            throw new ConverterException("id", (String) source, e.getMessage(), e);
        }
    }

    public static class ConverterException extends CipherException {

        @Getter
        final String filedName;

        public ConverterException(String fieldName, String source, String message, Throwable e) {
            super(source, message, e);
            this.filedName = fieldName;
        }
    }

    public static class ErrorProcessWevConversionService extends WebConversionService {

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

