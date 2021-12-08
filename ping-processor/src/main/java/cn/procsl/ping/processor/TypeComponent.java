package cn.procsl.ping.processor;

import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.Collections;

/**
 * 类型组件， 可以生成 特定的类型
 */
public interface TypeComponent extends Component<TypeSpec, TypeElement>, AnnotationComponentAware {

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
     * 获取field
     *
     * @return field列表
     */
    default Collection<FieldComponent> getFieldComponent() {
        return Collections.emptyList();
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

    /**
     * 获取方法组件
     *
     * @return 返回所有的方法组件
     */
    default Collection<MethodComponent> getMethodComponent() {
        return Collections.emptyList();
    }

}
