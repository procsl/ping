package cn.procsl.ping.boot.rest.hook;

import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author procsl
 * @date 2020/03/11
 */
public interface RequestMappingHook<T> {

    /**
     * 初始化方法
     *
     * @param mapping
     */
    void init(Map<T, HandlerMethod> mapping);

    /**
     * 创建映射的方法
     * 返回匹配的对象
     *
     * @param lookupPath
     * @param request
     * @return
     */
    List<HandlerMethod> mapping(String lookupPath, HttpServletRequest request);
}
