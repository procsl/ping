package cn.procsl.business.user.web.component;

import com.google.common.collect.ImmutableList;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author procsl
 * @date 2020/01/08
 */
public class DateConverter extends AbstractHttpMessageConverter<Date> implements InitializingBean {

    private SimpleDateFormat format;

    @Value("${ping.business.date.style:yyyy-MM-dd}")
    @Setter
    private String style;

    @Override
    protected boolean supports(Class<?> clazz) {
        return clazz == Date.class;
    }


    @Override
    protected Date readInternal(Class<? extends Date> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        InputStream is = inputMessage.getBody();
        if (is.available() >= 1024) {
            throw new HttpMessageNotReadableException("message to long", null, inputMessage);
        }
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        String source = new String(bytes);

        try {
            return format.parse(source);
        } catch (ParseException e) {
            throw new HttpMessageNotReadableException("Date convert error", e, inputMessage);
        }
    }

    @Override
    protected void writeInternal(Date date, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        MediaType content = outputMessage.getHeaders().getContentType();
        if (content == null) {
            outputMessage.getHeaders().setContentType(MediaType.TEXT_PLAIN);
        }
        String str = format.format(date);
        if (this.getDefaultCharset() == null) {
            outputMessage.getBody().write(str.getBytes());
        } else {
            outputMessage.getBody().write(str.getBytes(this.getDefaultCharset()));
        }
    }

    @Override
    protected MediaType getDefaultContentType(Date date) throws IOException {
        return MediaType.TEXT_PLAIN;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        format = new SimpleDateFormat(this.style);
        format.setLenient(false);
        super.setSupportedMediaTypes(ImmutableList.of(MediaType.ALL, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML));
    }
}
