package cn.procsl.business.user.web.component;

import cn.procsl.business.user.web.FilterPattern;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.google.common.collect.ImmutableList;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

        // 未解析到参数也跳过
        FilterPattern properties = this.once(this.request);
        if (properties == null) {
            super.serializeAsField(pojo, jgen, provider, writer);
            return;
        }

        List path = this.buildPath(jgen, writer);
        log.debug("path:{}", path);
        if (properties.matches(path)) {
            super.serializeAsField(pojo, jgen, provider, writer);
            return;
        }
    }

    protected List<String> buildPath(JsonGenerator jgen, PropertyWriter writer) {
        JsonStreamContext start;
        if (jgen.getOutputContext().hasCurrentIndex()) {
            start = jgen.getOutputContext().getParent();
        } else {
            start = jgen.getOutputContext();
        }

        if (start != null) {
            LinkedList<String> builder = new LinkedList<>();
            if (start.getCurrentName() != null) {
                builder.add(start.getCurrentName());
            }
            while (!start.inRoot() && (start = start.getParent()) != null) {
                if (start.inObject()) {
                    builder.add(start.getCurrentName());
                }
            }
            builder.add(0, writer.getName());
            Collections.reverse(builder);
            return builder;
        }
        return ImmutableList.of(writer.getName());
    }


    /**
     * 每个请求处理一次
     */
    protected FilterPattern once(HttpServletRequest request) {
        Object params = request.getAttribute(this.getClass().getName());
        if (params != null) {
            return (FilterPattern) params;
        }

        // 如果指定的参数不存在则跳过
        String filterParam = this.request.getParameter(this.field);
        if (StringUtils.isEmpty(filterParam)) {
            return null;
        }

        // 如果指定的类型为null, 则默认使用 include
        String type = this.request.getParameter(this.filterType);
        FilterPattern.PatternType filterType;
        if (StringUtils.isEmpty(type)) {
            filterType = FilterPattern.PatternType.include;
        } else {
            try {
                filterType = FilterPattern.PatternType.valueOf(type);
            } catch (Exception e) {
                filterType = FilterPattern.PatternType.include;
            }
        }

        // 解析参数
        FilterPattern properties = new FilterPattern(filterType, filterParam);
        this.request.setAttribute(this.getClass().getName(), properties);
        return properties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.field == null) {
            this.field = "field";
        }

        if (this.filterType == null) {
            this.filterType = "pattern";
        }
    }
}
