package cn.procsl.ping.web.serializable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
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
public class SerializableFilter extends SimpleBeanPropertyFilter implements InitializingBean {

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

        FilterPattern pattern = this.findPattern(pojo, this.request);

        if (pattern.skip(pojo, jgen, provider, writer)) {
            super.serializeAsField(pojo, jgen, provider, writer);
        }
    }


    /**
     * 每个请求处理一次
     */
    protected FilterPattern findPattern(Object pojo, HttpServletRequest request) {
        String key = this.getClass().getName();
        Object params = request.getAttribute(key);
        if (params != null) {
            return (FilterPattern) params;
        }

        // 如果存在该注解 则跳过以下的
        boolean bool = pojo.getClass().isAnnotationPresent(SkipFilter.class);
        if (bool) {
            return push(key, FilterPattern.compiler(), this.request);
        }

        // 如果指定的参数不存在则跳过

        String[] filterParams = this.request.getParameterValues(this.field);
        if (filterParams == null || filterParams.length == 0) {
            return push(key, FilterPattern.compiler(), this.request);
        }

        // 如果指定的类型为null, 则默认使用 include
        String type = this.request.getParameter(this.filterType);
        FilterPattern pattern = FilterPattern.compiler(getType(type), filterParams);
        return push(key, pattern, this.request);
    }

    private FilterPattern push(String key, FilterPattern pattern, HttpServletRequest request) {
        request.setAttribute(key, pattern);
        return pattern;
    }

    private FilterPattern.PatternType getType(String type) {

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
        return filterType;

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
