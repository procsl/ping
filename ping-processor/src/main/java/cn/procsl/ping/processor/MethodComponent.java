package cn.procsl.ping.processor;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.ExecutableElement;

public interface MethodComponent extends Component<MethodSpec, ExecutableElement>, AnnotationAware {

    /**
     * 设置方法参数组件
     *
     * @param composite 组件
     * @return 如果设置成功
     */
    boolean setParameterComposite(MethodParameterComposite composite);

    /**
     * 获取方法参数组件
     *
     * @param composite 方法参数组件
     * @return 方法参数组件
     */
    MethodParameterComposite getParameterComposite(MethodParameterComposite composite);

    /**
     * 设置方法返回值组件
     *
     * @param composite 方法返回值组件
     * @return 方法返回值组件
     */
    boolean setReturnComposite(MethodReturnComposite composite);

    /**
     * 获取方法返回值组件
     *
     * @param composite 方法返回值组件
     * @return 返回值组件
     */
    MethodReturnComposite getReturnComposite(MethodReturnComposite composite);

}
