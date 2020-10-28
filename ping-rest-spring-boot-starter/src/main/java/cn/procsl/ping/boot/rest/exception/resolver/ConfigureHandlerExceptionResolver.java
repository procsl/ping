package cn.procsl.ping.boot.rest.exception.resolver;

import cn.procsl.ping.boot.rest.config.RestWebProperties;
import cn.procsl.ping.boot.rest.exception.ExceptionCode;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 基于rest风格的全局异常处理
 *
 * @author procsl
 * @date 2019/12/25
 */
@Slf4j
@Order(Integer.MIN_VALUE + 1)
public class ConfigureHandlerExceptionResolver extends AbstractHandlerExceptionResolver {

    protected Map<String, String[]> conf;

    @SneakyThrows
    public ConfigureHandlerExceptionResolver() {
        @Cleanup
        @NonNull
        InputStream tmp = this.getClass().getClassLoader().getResourceAsStream("META-INF/exception.properties");

        Properties prop = new Properties();
        prop.load(tmp);

        conf = new HashMap<>(prop.size());
        prop.forEach((k, v) -> conf.put(k.toString(), v != null ? v.toString().split(":") : null));
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        String[] info = this.conf.get(ex.getClass().getName());
        if (info == null) {
            log.info("未匹配到异常:{}", ex.getClass());
            return null;
        }
        log.error("配置类异常处理:", ex);
        HttpStatus status = HttpStatus.valueOf(Integer.parseInt(info[0]));
        String code = info[1];
        String message = "true".equals(info[2]) ? info[3] : ex.getMessage();
        return createModelAndView(status, code, message);

    }


    private ModelAndView createModelAndView(HttpStatus status, String code, String message) {
        ModelAndView mv = new ModelAndView();
        ExceptionCode exp = new ExceptionCode();
        mv.addObject(RestWebProperties.modelKey, exp);
        mv.setStatus(status);
        exp.setCode(code);
        exp.setMessage(message);
        return mv;
    }


}
