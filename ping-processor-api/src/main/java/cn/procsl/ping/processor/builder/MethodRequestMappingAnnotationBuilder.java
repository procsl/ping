package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

@AutoService(AnnotationSpecBuilder.class)
public class MethodRequestMappingAnnotationBuilder
    extends AbstractRequestMappingAnnotationBuilder<MethodSpec.Builder> {

    @Override
    protected <E extends Element> void buildTargetAnnotation(ProcessorContext context, E source, MethodSpec.Builder target) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.bestGuess(this.requestMapping));

        String api = getPath("", source);

        builder.addMember("path", "$S", api.replaceAll("/$", ""));

        String method = getMethod((ExecutableElement) source);

        builder.addMember("method", "$N", method);

        AnnotationSpec tmp = builder.build();
        target.addAnnotation(tmp);
    }

    @Override
    protected Class<MethodSpec.Builder> target() {
        return MethodSpec.Builder.class;
    }

}
