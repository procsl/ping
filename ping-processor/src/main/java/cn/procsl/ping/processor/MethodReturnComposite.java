package cn.procsl.ping.processor;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;

/**
 * 方法返回值组件
 */
public interface MethodReturnComposite extends Component<TypeName, ExecutableElement>, AnnotationComponentAware {
}
