package cn.procsl.ping.processor.restful;

import cn.procsl.ping.processor.AnnotationDefinition;
import cn.procsl.ping.processor.FieldDefinition;
import cn.procsl.ping.processor.GeneratorDefinition;

import javax.lang.model.element.Element;
import java.util.Collection;

public interface ControllerDefinition extends GeneratorDefinition {

    @Override
    default String generatorType() {
        return "SpringMVC-Controller";
    }

    /**
     * 返回控制器名称, 以小写字符开头
     *
     * @return 控制器名称
     */
    String getControllerName();

    /**
     * 是否生成接口
     *
     * @return 生成接口
     */
    boolean isGeneratorInterface();

    /**
     * 获取被参照的element
     *
     * @return 如果存在被参照的element, 则返回
     */
    Element getTarget();


    /**
     * 获取字段
     *
     * @return 属性字段
     */
    Collection<FieldDefinition> getFields();

    /**
     * 获取当前类的注解
     *
     * @return 注解
     */
    Collection<AnnotationDefinition> getAnnotations();

    /**
     * 获取方法
     *
     * @return rest方法
     */
    Collection<RestfulMethodDefinition> getRestfulMethod();

}
