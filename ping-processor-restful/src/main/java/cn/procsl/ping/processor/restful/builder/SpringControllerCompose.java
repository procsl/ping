package cn.procsl.ping.processor.restful.builder;

import cn.procsl.ping.processor.ProcessorContext;
import cn.procsl.ping.processor.component.AbstractComponent;
import cn.procsl.ping.processor.component.TypeComponent;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

public class SpringControllerCompose extends AbstractComponent<TypeSpec, TypeElement> implements TypeComponent<TypeElement> {

    String createClassName(TypeElement element) {
        String name = element.getSimpleName().toString();
        if (name.endsWith("Service")) {
            name = name.replaceAll("Application", "").replaceAll("Service" + "$", "Controller");
        } else {
            name = name + "Controller";
        }
        return name;
    }


    @Override
    public TypeSpec generateStruct(ProcessorContext context, TypeElement element) {
        TypeSpec.Builder type = TypeSpec.classBuilder(this.createClassName(element));

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
}
