package cn.procsl.business.user.web.component;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * @author procsl
 * @date 2020/01/07
 */
public class NumberHttpMessageConverter extends AbstractHttpMessageConverter<Number> implements InitializingBean {

    @Override
    protected boolean supports(Class<?> clazz) {
        return Number.class.isAssignableFrom(clazz);
    }

    @Override
    protected Number readInternal(Class<? extends Number> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        InputStream is = inputMessage.getBody();
        if (is.available() >= 1024) {
            throw new HttpMessageNotReadableException("message to long", null, inputMessage);
        }
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        String source = new String(bytes);
        try {
            if (Integer.class.isAssignableFrom(clazz)) {
                return Integer.parseInt(source);
            }

            if (Long.class.isAssignableFrom(clazz)) {
                return Long.parseLong(source);
            }

            if (Double.class.isAssignableFrom(clazz)) {
                return Double.parseDouble(source);
            }

            if (Float.class.isAssignableFrom(clazz)) {
                return Float.parseFloat(source);
            }

            if (BigInteger.class.isAssignableFrom(clazz)) {
                return new BigInteger(bytes);
            }

            if (BigDecimal.class.isAssignableFrom(clazz)) {
                return new BigDecimal(source);
            }

            if (Short.class.isAssignableFrom(clazz)) {
                return Short.parseShort(source);
            }

            if (Byte.class.isAssignableFrom(clazz)) {
                return Byte.parseByte(source);
            }

        } catch (NumberFormatException e) {
            throw new HttpMessageNotReadableException(source + " can't read", e, inputMessage);
        }

        throw new HttpMessageNotReadableException("Not Supported type: " + clazz.getName(), null, inputMessage);
    }

    @Override
    protected MediaType getDefaultContentType(Number number) throws IOException {
        return MediaType.TEXT_PLAIN;
    }

    @Override
    protected void writeInternal(Number number, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        outputMessage.getBody().write(number.byteValue());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.setSupportedMediaTypes(ImmutableList.of(MediaType.ALL, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML));
    }
}
