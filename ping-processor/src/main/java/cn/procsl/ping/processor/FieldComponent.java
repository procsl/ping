package cn.procsl.ping.processor;

import com.squareup.javapoet.FieldSpec;

import javax.lang.model.element.TypeElement;

public interface FieldComponent extends Component<FieldSpec, TypeElement>, AnnotationComponentAware {
}
