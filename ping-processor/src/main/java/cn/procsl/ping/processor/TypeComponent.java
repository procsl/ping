package cn.procsl.ping.processor;

import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

public interface TypeComponent extends Component<TypeSpec, TypeElement>, AnnotationAware<TypeElement> {

    /**
     * 添加字段组件
     *
     * @param fieldComponent 字段生成组件
     * @return 如果添加失败则返回false
     */
    default boolean addFieldComponent(FieldComponent fieldComponent) {
        return false;
    }

    /**
     * 方法字段组件
     *
     * @param methodComponent 方法生成组件
     * @return 如果添加失败则返回false
     */
    default boolean addMethodComponent(MethodComponent methodComponent) {
        return false;
    }

}
