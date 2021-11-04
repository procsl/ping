package cn.procsl.ping.processor.component;

import cn.procsl.ping.processor.Component;
import cn.procsl.ping.processor.ProcessorContext;
import com.squareup.javapoet.FieldSpec;

public interface FieldComponent<E> extends Component<FieldSpec, E>, AnnotationAware<E> {
}
