package cn.procsl.ping.processor.api.builder.spring;

import cn.procsl.ping.processor.api.AnnotationVisitor;
import cn.procsl.ping.processor.api.AnnotationVisitorLoader;
import cn.procsl.ping.processor.api.Generator;
import cn.procsl.ping.processor.api.ProcessorContext;
import cn.procsl.ping.processor.api.utils.NamingUtils;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import lombok.RequiredArgsConstructor;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;


@RequiredArgsConstructor
public class ServiceFieldBuilder implements Generator<FieldSpec> {

    final TypeElement targetElement;

    final AnnotationVisitorLoader visitor;


    public ServiceFieldBuilder(TypeElement targetElement, ProcessorContext context) {
        this.targetElement = targetElement;
        this.visitor = new AnnotationVisitorLoader(context, AnnotationVisitor.SupportType.CONTROLLER);
    }

    @Override
    public FieldSpec getSpec() {

        String fieldName = NamingUtils.lowerCamelCase(targetElement.getSimpleName().toString());
        FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(TypeName.get(targetElement.asType()), fieldName, Modifier.PROTECTED);

        visitor.fieldVisitor(targetElement, fieldSpecBuilder);
        return fieldSpecBuilder.build();
    }
}
