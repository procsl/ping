package cn.procsl.ping.boot.web.cipher.id;

import cn.procsl.ping.boot.web.cipher.CipherLockupService;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.core.convert.converter.GenericConverter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Slf4j
public final class CipherSecurityBuilder {


    public static GenericConverter buildConverter(CipherLockupService server) {
        return new CipherGenericConverter(new SecurityIdCipherService(server));
    }

    public static AnnotationIntrospector buildJacsonIntrospector(CipherLockupService service) {
        return new JacksonSecurityIdAnnotationIntrospector(new SecurityIdCipherService(service));
    }

    public static WebConversionService hookMvcConversionService(WebConversionService conversionService) throws IllegalAccessException {

        log.trace("拦截WebConversionService实例,添加钩子");
        var wrapper = new CipherGenericConverter.ErrorProcessWevConversionService();

        var targetStart = wrapper.getClass().getSuperclass();
        do {
            copyProperties(conversionService, wrapper, targetStart);
            targetStart = targetStart.getSuperclass();
        } while (targetStart != null);

        return wrapper;
    }


    private static void copyProperties(Object source, Object target, Class<?> clazz) throws IllegalAccessException {
        Field[] fields = clazz.getDeclaredFields();
        for (Field sourceField : fields) {

            if (Modifier.isStatic(sourceField.getModifiers())) {
                log.trace("跳过static属性: {}", sourceField.getName());
                continue;
            }

            boolean able = sourceField.trySetAccessible();
            if (!able) {
                sourceField.setAccessible(true);
            }
            Object value = sourceField.get(source);
            sourceField.set(target, value);
            if (!able) {
                sourceField.setAccessible(false);
            }
        }
    }

}
