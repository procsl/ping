package cn.procsl.ping.processor.restful.builder.component;

import cn.procsl.ping.processor.ProcessorContext;
import cn.procsl.ping.processor.component.AbstractComponent;
import cn.procsl.ping.processor.component.AnnotationComponent;
import cn.procsl.ping.processor.component.FieldComponent;
import cn.procsl.ping.processor.restful.utils.NamingUtils;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class ServiceTypeFieldComponent extends AbstractComponent<FieldSpec, TypeElement> implements FieldComponent<TypeElement> {

    @Override
    public FieldSpec generateStruct(ProcessorContext context, TypeElement element) {
        TypeName type = TypeName.get(element.asType());
        String name = NamingUtils.lowerCamelCase(element.getSimpleName().toString());
        FieldSpec.Builder builder = FieldSpec.builder(type, name, Modifier.PRIVATE);

        this.getChildren().forEach(item -> {
            if (item instanceof AnnotationComponent) {
                Object result = item.generateStruct(context, element);
                builder.addAnnotation((AnnotationSpec) result);
            }
        });

        return builder.build();
    }

}
