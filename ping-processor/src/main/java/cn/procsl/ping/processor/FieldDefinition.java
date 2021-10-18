package cn.procsl.ping.processor;

import java.util.Collection;

public interface FieldDefinition extends GeneratorDefinition {

    /**
     * 小驼峰命名法
     *
     * @return 字段名称
     */
    String getFieldName();

    /**
     * 是否为生成的类型
     *
     * @return 如果为当前生成的类型
     */
    String isGenerated();

    /**
     * 获取类型
     *
     * @return 返回当前属性的类型
     */
    TypeNameDefinition getType();

    /**
     * 获取当前field的注解
     *
     * @return 注解
     */
    Collection<AnnotationDefinition> getAnnotations();


}
