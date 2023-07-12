package cn.procsl.ping.boot.web.encrypt;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Slf4j
public class SecurityIDAnnotationIntrospector extends JacksonAnnotationIntrospector {

    @Setter
    ApplicationContext applicationContext;

    @Override
    public Object findDeserializer(Annotated a) {


        SecurityId security = a.getAnnotation(SecurityId.class);
        if (security != null) {
            return new DecryptProcessor(security, findService());
        }

        boolean bool = Long.TYPE == a.getRawType();
        if (!bool) {
            return super.findDeserializer(a);
        }

        try {
            RequestAttributes attr = RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                security = (SecurityId) attr.getAttribute(SecurityId.class.getName(), RequestAttributes.SCOPE_REQUEST);
                if (security != null) {
                    log.info("找到了");
                    return new DecryptProcessor(security, findService());
                }
            }
        } catch (BeansException exception) {
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
            return new EncryptProcessor(security, findService());
        }
        return super.findSerializer(a);
    }

    private EncryptDecryptService findService() {
        return this.applicationContext.getBean(EncryptDecryptService.class);
    }


}
