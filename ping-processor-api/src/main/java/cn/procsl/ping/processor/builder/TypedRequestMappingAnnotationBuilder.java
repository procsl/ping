package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.ProcessorContext;
import cn.procsl.ping.processor.generator.TypeAnnotationSpecBuilder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

@AutoService(TypeAnnotationSpecBuilder.class)
public class TypedRequestMappingAnnotationBuilder extends AbstractRequestMappingAnnotationBuilder implements TypeAnnotationSpecBuilder {

    @Override
    public AnnotationSpec build(ProcessorContext context, TypeElement source) {

        AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.bestGuess(this.requestMapping));

        String prefix = context.getConfig("api.prefix");
        String api = getPath(prefix, source);

        builder.addMember("path", "$S", api);
        return builder.build();
    }

}
