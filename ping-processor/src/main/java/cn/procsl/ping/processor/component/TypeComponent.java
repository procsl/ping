package cn.procsl.ping.processor.component;

import cn.procsl.ping.processor.Component;
import cn.procsl.ping.processor.ProcessorContext;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public interface TypeComponent<E> extends Component<TypeSpec, E> {


    @Override
    default TypeSpec generateStruct(ProcessorContext context, E element) {
        TypeSpec.Builder type = this.builder(context, element);

        this.getChildren().forEach(item -> {
            Object result = item.generateStruct(context, element);
            if (result instanceof AnnotationSpec) {
                type.addAnnotation((AnnotationSpec) result);
                return;
            }

            if (result instanceof FieldSpec) {
                type.addField((FieldSpec) result);
                return;
            }

            if (result instanceof MethodSpec) {
                type.addMethod((MethodSpec) result);
            }

        });

        return type.build();
    }

    TypeSpec.Builder builder(ProcessorContext context, E element);

}
