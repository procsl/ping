package cn.procsl.business.user.web;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author procsl
 * @date 2020/02/07
 */
public class FilterPattern {

    protected PatternType pattern;

    protected Map<String, Object> fields;

    public enum PatternType {
        include, exclude
    }

    public FilterPattern(PatternType pattern, String params) {

        this.pattern = pattern;
        String[] tmp = params.split(",");
        fields = new HashMap<>(tmp.length);
        for (String param : tmp) {
            if (!StringUtils.isEmpty(param)) {
                this.resolve(fields, param.split("\\."));
            }
        }

    }

    private void resolve(Map<String, Object> map, String... param) {
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
        this.resolve(tmpMap, tmp);
    }

    /**
     * @param path three.list.test
     * @return
     */
    public boolean matches(List<String> path) {
        switch (pattern) {
            case include:
                return include(this.fields, path);
            case exclude:
                return exclude(this.fields, path);
        }
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
}
