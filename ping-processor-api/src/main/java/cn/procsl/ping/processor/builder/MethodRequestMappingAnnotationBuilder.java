package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.GeneratorContext;
import cn.procsl.ping.processor.generator.MethodAnnotationSpecBuilder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.ExecutableElement;

@AutoService(MethodAnnotationSpecBuilder.class)
public class MethodRequestMappingAnnotationBuilder extends AbstractRequestMappingAnnotationBuilder implements MethodAnnotationSpecBuilder {


    @Override
    public AnnotationSpec build(GeneratorContext context, ExecutableElement source) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.bestGuess(this.requestMapping));

        String api = getPath("", source);

        builder.addMember("path", api);

        String method = getMethod(source);

        builder.addMember("method", method);

        return builder.build();
    }
}
