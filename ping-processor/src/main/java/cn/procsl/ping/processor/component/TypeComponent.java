package cn.procsl.ping.processor.component;

import cn.procsl.ping.processor.Component;
import cn.procsl.ping.processor.ProcessorContext;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public interface type<E> extends Component<TypeSpec, E>, AnnotationAware<E> {

    /**
     * 添加字段组件
     *
     * @param fieldComponent 字段生成组件
     * @return 如果添加失败则返回false
     */
    default boolean addFieldComponent(FieldComponent<E> fieldComponent) {
        return false;
    }

    /**
     * 方法字段组件
     *
     * @param methodComponent 方法生成组件
     * @return 如果添加失败则返回false
     */
    default boolean addMethodComponent(MethodComponent<E> methodComponent) {
        return false;
    }

}
