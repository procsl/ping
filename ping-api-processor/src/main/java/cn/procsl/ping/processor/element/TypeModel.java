package cn.procsl.ping.processor.element;

import java.util.Collection;

public interface TypeModel extends NamingModel {

    /**
     * 获取字段
     *
     * @return 获取当前类的字段
     */
    Collection<FieldModel> getFields();

    /**
     * 获取方法
     *
     * @return 获取当前类的方法
     */
    Collection<MethodModel> getMethods();

    /**
     * 获取当前类的注解
     *
     * @return 获取当前类的注解
     */
    Collection<AnnotationModel> getAnnotations();
}
