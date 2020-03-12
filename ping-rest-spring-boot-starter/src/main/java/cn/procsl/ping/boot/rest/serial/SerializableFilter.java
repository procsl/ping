package cn.procsl.ping.boot.rest.serial;

import cn.procsl.ping.boot.rest.annotation.SkipFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static cn.procsl.ping.boot.rest.serial.FilterPattern.compiler;

/**
 * @author procsl
 * @date 2020/02/05
 */
@Slf4j
@RequiredArgsConstructor
public class SerializableFilter extends SimpleBeanPropertyFilter {

    final static public String FILTER_ID = "filter-id";

    private final FilterContext context;

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {

        FilterPattern pattern = this.findPattern(pojo);

        if (pattern.skip(pojo, jgen, provider, writer)) {
            super.serializeAsField(pojo, jgen, provider, writer);
        }
    }


    /**
     * 每个请求处理一次
     */
    protected FilterPattern findPattern(Object pojo) {
        FilterPattern params = context.getFilterPattern();
        if (params != null) {
            return params;
        }

        // 如果存在该注解 则跳过以下的
        boolean bool = pojo.getClass().isAnnotationPresent(SkipFilter.class);
        if (bool) {
            this.context.setFilterPattern(compiler());
            return compiler();
        }

        // 如果指定的参数不存在则跳过
        String[] filterParams = context.filterParams();
        if (filterParams == null || filterParams.length == 0) {
            this.context.setFilterPattern(compiler());
            return compiler();
        }

        // 如果指定的类型为null, 则默认使用 include
        FilterPattern pattern = compiler(context.filterType(), filterParams);
        context.setFilterPattern(pattern);
        return pattern;
    }

//    private FilterPattern.PatternType getType(String type) {
//
//        FilterPattern.PatternType filterType;
//        if (StringUtils.isEmpty(type)) {
//            filterType = FilterPattern.PatternType.include;
//        } else {
//            try {
//                filterType = FilterPattern.PatternType.valueOf(type);
//            } catch (Exception e) {
//                filterType = FilterPattern.PatternType.include;
//            }
//        }
//        return filterType;
//
//    }

}
