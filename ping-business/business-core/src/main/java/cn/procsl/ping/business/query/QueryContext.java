package cn.procsl.ping.business.query;

import java.util.List;

/**
 * 查询上下文
 *
 * @author procsl
 * @date 2019/12/13
 */
public class QueryContext<T> {

    /**
     * 大小
     */
    private Integer size;

    /**
     * 偏移量
     */
    private Integer offset;

    /**
     * 限制条数
     */
    private Integer limit;

    /**
     * 排序的字段
     */
    private List<String> sort;

    /**
     * 查询对象
     */
    private T query;

}
