package cn.procsl.ping.processor;

import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.element.ExecutableElement;
import java.util.List;


/**
 * 方法参数组件
 */
public interface MethodParameterComposite extends Component<List<ParameterSpec>, ExecutableElement>, AnnotationAware {

}
