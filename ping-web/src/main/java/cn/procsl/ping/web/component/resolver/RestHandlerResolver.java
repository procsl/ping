package cn.procsl.ping.web.component.resolver;

import cn.procsl.ping.web.annotation.Accepted;
import cn.procsl.ping.web.annotation.Created;
import cn.procsl.ping.web.annotation.NoContent;
import cn.procsl.ping.web.component.converter.DateHttpMessageConverter;
import cn.procsl.ping.web.component.converter.NumberHttpMessageConverter;
import cn.procsl.ping.web.component.view.Constant;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author procsl
 * @date 2019/12/29
 */
@Slf4j
public class RestHandlerResolver implements HandlerMethodReturnValueHandler, Ordered, InitializingBean {

    @Setter
    @Getter
    int order;

    @Setter
    private RequestResponseBodyMethodProcessor process;

    @Autowired(required = false)
    private List<HttpMessageConverter<?>> httpMessageConverters;

    /**
     * 拦截所有的类型
     *
     * @param returnType
     * @return
     */
    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return true;
    }

    @Override
    public void handleReturnValue(Object returnValue,
                                  MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest) throws Exception {

        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        // 判断是否为简单类型,如果是则直接根据相关的注解返回
        if (!isSimpleType(returnType)) {
            mavContainer.addAttribute(Constant.RETURN_VALUE.getValue(), returnValue);
            return;
        }

        do {

            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if (HttpMethod.GET.matches(request.getMethod())) {
                break;
            }

            // 如果存在此注解或者为Void类型, 直接返回
            if ("void".equals(returnType.getParameterType().getName()) || Void.class.isAssignableFrom(returnType.getParameterType()) || returnType.hasMethodAnnotation(NoContent.class)) {
                response.setStatus(HttpStatus.NO_CONTENT.value());
                this.process.handleReturnValue(null, returnType, mavContainer, webRequest);
                return;
            }

            if (returnType.hasMethodAnnotation(Accepted.class)) {
                response.setStatus(HttpStatus.ACCEPTED.value());
                break;
            }

            // 如果为Created则可以生成Location
            if (!returnType.hasMethodAnnotation(Created.class)) {
                break;
            }

            response.setStatus(HttpStatus.CREATED.value());
            Created create = returnType.getMethodAnnotation(Created.class);
            String location = create.location();
            if (location == null || location.isEmpty()) {
                log.debug("Location is null. skip");
                break;
            }

            String id = this.convert(returnValue, returnType);
            if (id == null || id.isEmpty()) {
                log.debug("Id is null. skip");
                break;
            }

            String link = replace(create.name(), location, id);
            response.setHeader("Location", link);
        } while (false);
        this.process.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }

    private boolean isSimpleType(MethodParameter returnType) throws IOException {
        return Void.class.isAssignableFrom(returnType.getParameterType()) ||
                String.class.isAssignableFrom(returnType.getParameterType()) ||
                Date.class.isAssignableFrom(returnType.getParameterType()) ||
                Number.class.isAssignableFrom(returnType.getParameterType());
    }

    protected String convert(Object returnValue, MethodParameter returnType) {
        if (returnType.getParameterType() == String.class) {
            return (String) returnValue;
        }

        if (returnValue instanceof Integer) {
            return String.valueOf(returnValue);
        }

        if (returnValue instanceof Long) {
            return String.valueOf(returnValue);
        }

        if (returnValue instanceof Character) {
            return String.valueOf(returnValue);
        }
        return null;
    }

    protected String replace(String name, String old, String newStr) {
        if (name == null || name.isEmpty()) {
            return old;
        }
        return old.replace("{" + name + "}", newStr);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.process == null) {
            final List<HttpMessageConverter<?>> list;
            if (this.httpMessageConverters == null || this.httpMessageConverters.isEmpty()) {
                list = new LinkedList<>();
                list.add(new StringHttpMessageConverter());
                list.add(new NumberHttpMessageConverter());
                list.add(new DateHttpMessageConverter());
            } else {
                list = this.httpMessageConverters;
            }
            this.process = new RequestResponseBodyMethodProcessor(list);
        }
    }

}
