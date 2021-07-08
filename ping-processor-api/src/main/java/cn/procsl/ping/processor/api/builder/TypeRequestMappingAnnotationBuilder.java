package cn.procsl.ping.processor.api.builder;

import cn.procsl.ping.processor.api.AnnotationSpecBuilder;
import cn.procsl.ping.processor.api.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;

@AutoService(AnnotationSpecBuilder.class)
public class TypeRequestMappingAnnotationBuilder
    extends AbstractRequestMappingAnnotationBuilder<TypeSpec.Builder> {

    final String validate = "org.springframework.validation.annotation.Validated";

    @Override
    protected <E extends Element> void buildTargetAnnotation(ProcessorContext context, E source, TypeSpec.Builder target) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.bestGuess(this.requestMapping));

        String prefix = context.getConfig("processor.api.prefix");
        String api = getPath(prefix, source);

        builder.addMember("path", "$S", api);
        target.addAnnotation(builder.build());

        AnnotationSpec validateAnnotation = AnnotationSpec.builder(ClassName.bestGuess(validate)).build();
        target.addAnnotation(validateAnnotation);

    }

    @Override
    protected Class<TypeSpec.Builder> target() {
        return TypeSpec.Builder.class;
    }
}
