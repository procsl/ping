package cn.procsl.ping.boot.rest.serial;

/**
 * @author procsl
 * @date 2020/03/08
 */
public interface FilterContext {

    /**
     * 获取上下文的匹配器
     *
     * @return
     */
    FilterPattern getFilterPattern();

    /**
     * 设置上下文匹配器
     *
     * @param pattern
     */
    void setFilterPattern(FilterPattern pattern);

    /**
     * 匹配数据
     *
     * @return
     */
    String[] filterParams();

    /**
     * 拦截类型
     *
     * @return
     */
    FilterPattern.PatternType filterType();
}
