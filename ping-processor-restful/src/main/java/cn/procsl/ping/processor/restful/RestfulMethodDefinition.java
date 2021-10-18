package cn.procsl.ping.processor.restful;

import cn.procsl.ping.processor.AnnotationDefinition;
import cn.procsl.ping.processor.GeneratorDefinition;

import java.util.Collection;

public interface RestfulMethodDefinition extends GeneratorDefinition {

    /**
     * 获取当前方法的注解
     *
     * @return 注解
     */
    Collection<AnnotationDefinition> getAnnotations();

    /**
     * 获取方法名称
     *
     * @return 方法名称
     */
    String getName();


}
