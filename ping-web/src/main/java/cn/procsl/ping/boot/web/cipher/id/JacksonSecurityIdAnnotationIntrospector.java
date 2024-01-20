package cn.procsl.ping.boot.web.cipher.id;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;


@Slf4j
final class JacksonSecurityIdAnnotationIntrospector extends JacksonAnnotationIntrospector {

    final SecurityIdCipherService cipherService;

    public JacksonSecurityIdAnnotationIntrospector(SecurityIdCipherService securityIdCipherService) {
        this.cipherService = securityIdCipherService;
    }

    @Override
    public Object findDeserializer(Annotated a) {


        SecurityId security = a.getAnnotation(SecurityId.class);
        if (security != null) {
            return new JacksonCipherDecodeDeserializer(security, cipherService);
        }

        boolean bool = Long.TYPE == a.getRawType() || Long.class == a.getRawType();
        if (!bool) {
            return super.findDeserializer(a);
        }

        RequestAttributes attr = RequestContextHolder.getRequestAttributes();
        if (attr == null) {
            return super.findDeserializer(a);
        }

        Object handler = attr.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        if (handler == null) {
            return super.findDeserializer(a);
        }

        if (!(handler instanceof HandlerMethod)) {
            return super.findDeserializer(a);
        }
        for (MethodParameter parameter : ((HandlerMethod) handler).getMethodParameters()) {
            boolean isAssignable = Iterable.class.isAssignableFrom(parameter.getParameterType());
            if (!isAssignable) {
                continue;
            }

            security = parameter.getParameterAnnotation(SecurityId.class);
            if (security == null) {
                continue;
            }

            RequestBody body = parameter.getParameterAnnotation(RequestBody.class);
            if (body == null) {
                continue;
            }

            return new JacksonCipherDecodeDeserializer(security, this.cipherService);
        }

        return super.findDeserializer(a);
    }

    @Override
    public Object findDeserializationContentConverter(AnnotatedMember a) {
        return super.findDeserializationContentConverter(a);
    }


    @Override
    public Object findSerializer(Annotated a) {
        SecurityId security = a.getAnnotation(SecurityId.class);
        if (security != null) {
            return new JacksonCipherEncodeSerializer(security, cipherService);
        }
        return super.findSerializer(a);
    }


}
