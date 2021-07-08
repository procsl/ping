package cn.procsl.ping.processor.api.builder;

import cn.procsl.ping.processor.api.AbstractAnnotationSpecBuilder;
import cn.procsl.ping.processor.api.AnnotationSpecBuilder;
import cn.procsl.ping.processor.api.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;

import javax.lang.model.element.Element;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(AnnotationSpecBuilder.class)
public class DTOFieldAnnotationSpecBuilder extends AbstractAnnotationSpecBuilder<FieldSpec.Builder> {

    @Override
    protected boolean isType(String type) {
        return "DTO".equals(type);
    }

    @Override
    protected <E extends Element> void buildTargetAnnotation(ProcessorContext context, E source, FieldSpec.Builder target) {
        Set<AnnotationSpec> annotations = source.getAnnotationMirrors()
            .stream()
            .filter(item -> item.getAnnotationType().toString().startsWith("javax.validation"))
            .map(AnnotationSpec::get)
            .collect(Collectors.toSet());
        target.addAnnotations(annotations);
    }

    @Override
    protected Class<FieldSpec.Builder> target() {
        return FieldSpec.Builder.class;
    }
}
