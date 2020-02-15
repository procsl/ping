package cn.procsl.ping.web.serializable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author procsl
 * @date 2020/02/07
 */
@Slf4j
public class FilterPattern {

    protected PatternType patternType;

    protected Map<String, Object> fields;

    protected static final FilterPattern TRUE = new InnerFilter(true);

    protected static final FilterPattern FALSE = new InnerFilter(false);

    public enum PatternType {
        include, exclude, skip
    }

    public static FilterPattern compile() {
        return TRUE;
    }

    public static FilterPattern compile(PatternType patternType, String params) {
        if (patternType == PatternType.skip) {
            return TRUE;
        }

        boolean bool = patternType == PatternType.include && (params == null || params.isEmpty());
        if (bool) {
            return FALSE;
        }

        bool = patternType == PatternType.exclude && (params == null || params.isEmpty());
        if (bool) {
            return TRUE;
        }

        FilterPattern pattern = new FilterPattern();
        pattern.patternType = patternType;
        String[] tmp = params.split(",");
        pattern.fields = new HashMap<>(tmp.length);
        for (String param : tmp) {
            if (param.isEmpty()) {
                continue;
            }
            resolve(pattern.fields, param.split("\\."));
        }

        return pattern;
    }

    private static void resolve(Map<String, Object> map, String... param) {
        if (param.length == 0) {
            return;
        }
        if (param.length == 1) {
            map.put(param[0], null);
            return;
        }
        Object object = map.get(param[0]);
        if (object == null) {
            object = new LinkedHashMap<>(1);
        }
        Map<String, Object> tmpMap = (Map<String, Object>) object;
        map.put(param[0], tmpMap);
        String[] tmp = new String[param.length - 1];
        for (int i = 0; i < param.length - 1; i++) {
            tmp[i] = param[i + 1];
        }
        resolve(tmpMap, tmp);
    }

    public boolean skip(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) {
        switch (patternType) {
            case skip:
                return true;
            case include:
                Object object = buildPath(jgen, writer);
                if (object instanceof String) {
                    return include(this.fields, (String) object);
                }
                return include(this.fields, (List<String>) object);
            case exclude:
                object = buildPath(jgen, writer);
                if (object instanceof String) {
                    return exclude(this.fields, (String) object);
                }
                return exclude(this.fields, (List<String>) object);
        }
        return true;
    }

    protected static boolean exclude(Map<String, Object> map, String key) {
        boolean bool = map.containsKey(key);
        // map中存在该节点
        if (bool) {
            // 最后的节点
            if (map.get(key) == null) {
                return false;
            }
            // 非最后的节点, 说明map还有子节点
            return true;
        }

        // map中不存在该节点
        return true;
    }

    /**
     * 只要有子节点就一直遍历
     *
     * @param map
     * @param path
     * @return
     */
    protected static boolean exclude(Map<String, Object> map, List<String> path) {

        String key = path.get(0);
        if (path.size() == 1) {
            return exclude(map, path.get(0));
        }

        // 如果不是最后一个, 首先判断是否存在在map中
        if (!map.containsKey(key)) {
            // 如果不存在
            return true;
        }

        // 判断是否子节点有效
        Object tmpMap = null;
        if ((tmpMap = map.get(key)) == null) {
            return false;
        }

        return exclude((Map<String, Object>) tmpMap, path.subList(1, path.size()));
    }

    /**
     * @param map  {
     *             "one":"123",
     *             "two":"234,
     *             "three":{
     *             "test":"809",
     *             "hello":"world"
     *             },
     *             }
     * @param path three
     *             <p>
     *             通过path匹配 如果path匹配完了 则返回true
     *             如果path 未匹配完成 map为空 则返回false
     * @return
     */
    protected static boolean include(Map<String, Object> map, List<String> path) {
        if (path.isEmpty()) {
            return false;
        }
        String tmp = path.get(0);
        if (path.size() == 1) {
            return map.containsKey(tmp);
        }

        if (!map.containsKey(tmp)) {
            return false;
        }

        Object tmpMap;
        if ((tmpMap = map.get(tmp)) == null) {
            return true;
        }
        return include((Map<String, Object>) tmpMap, path.subList(1, path.size()));
    }

    /**
     * @param map
     * @param path
     * @return
     */
    protected boolean include(Map<String, Object> map, String path) {
        return map.containsKey(path);
    }

    protected Object buildPath(JsonGenerator jgen, PropertyWriter writer) {
        JsonStreamContext start;
        if (jgen.getOutputContext().hasCurrentIndex()) {
            start = jgen.getOutputContext().getParent();
        } else {
            start = jgen.getOutputContext();
        }

        if (start == null) {
            return writer.getName();
        }

        LinkedList<String> builder = null;
        if (start.getCurrentName() != null) {
            builder = this.createAndPush(null, start.getCurrentName());
        }

        while (!start.inRoot() && (start = start.getParent()) != null) {
            if (start.inObject()) {
                builder = this.createAndPush(builder, start.getCurrentName());
            }
        }

        if (builder != null) {
            builder.add(0, writer.getName());
            Collections.reverse(builder);
           return builder;
        }

        return writer.getName();
    }

    private LinkedList<String> createAndPush(LinkedList<String> defaultList, String item) {
        if (defaultList == null) {
            defaultList = new LinkedList<>();
        }
        defaultList.add(item);
        return defaultList;
    }

    private static class InnerFilter extends FilterPattern {

        private boolean bool;

        public InnerFilter(boolean bool) {
            this.bool = bool;
        }

        @Override
        public final boolean skip(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) {
            return bool;
        }
    }
}
