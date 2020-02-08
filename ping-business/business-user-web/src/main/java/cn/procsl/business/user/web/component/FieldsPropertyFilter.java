package cn.procsl.business.user.web.component;

import cn.procsl.business.user.web.FilterProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.google.common.collect.ImmutableSet;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author procsl
 * @date 2020/02/05
 */
@Slf4j
public class FieldsPropertyFilter extends SimpleBeanPropertyFilter implements InitializingBean {

    final static public String FILTER_ID = "filter-id";

    @Autowired
    protected HttpServletRequest request;

    /**
     * 拦截字段
     */
    @Setter
    protected String field;

    /**
     * 拦截类型
     */
    @Setter
    private String filterType;

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {

        // 如果该类存在过指定的注解, 则直接跳过
        if (pojo.getClass().isAnnotationPresent(SkipFilter.class)) {
            super.serializeAsField(pojo, jgen, provider, writer);
            return;
        }

        FilterProperties properties = this.process(this.request);
        if (properties == null) {
            super.serializeAsField(pojo, jgen, provider, writer);
            return;
        }

        // 构建路径
        String path = jgen.getOutputContext().pathAsPointer().toString() + "/" + writer.getName();
        path = path.substring(1).replace("/", ".");

        log.debug("path:{}", path);
        if (properties.matches(path)) {
            super.serializeAsField(pojo, jgen, provider, writer);
            return;
        }
    }



    /**
     * 每个请求处理一次
     */
    protected FilterProperties process(HttpServletRequest request) {
        Object params = request.getAttribute(this.getClass().getName());
        if (params != null) {
            return (FilterProperties) params;
        }

        // 如果指定的参数不存在则跳过
        String filterParam = this.request.getParameter(this.field);
        if (StringUtils.isEmpty(filterParam)) {
            return null;
        }

        FilterProperties properties = new FilterProperties();

        // 如果指定的类型为null, 则默认使用 include
        String type = this.request.getParameter(this.filterType);
        if (StringUtils.isEmpty(type)) {
            properties.setType(FilterProperties.FilterType.INCLUDE);
        } else {
            try {
                properties.setType(FilterProperties.FilterType.valueOf(type));
            } catch (Exception e) {
                properties.setType(FilterProperties.FilterType.INCLUDE);
            }
        }

        // 解析参数
        ImmutableSet<String> set = ImmutableSet.copyOf(filterParam.split(","));
        properties.setFields(set);

        this.request.setAttribute(this.getClass().getName(), properties);
        return properties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.field == null) {
            this.field = "field";
        }

        if (this.filterType == null) {
            this.filterType = "filter";
        }
    }
}
