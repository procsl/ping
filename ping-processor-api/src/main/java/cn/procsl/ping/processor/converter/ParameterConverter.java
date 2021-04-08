package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.FieldModel;
import cn.procsl.ping.processor.model.NamingModel;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.stream.Collectors;

@SuperBuilder
public class ParameterConverter extends AbstractAwareConvertor<FieldModel, ParameterSpec> {

    @NonNull
    private final ModelConverter<NamingModel, TypeName> namingModelTypeNameConverter;

    @NonNull
    private final ModelConverter<AnnotationModel, AnnotationSpec> annotationModelToAnnotationSpecConverter;

    @Override
    protected ParameterSpec convertTo(FieldModel source) {

        ParameterSpec.Builder build = ParameterSpec.builder(namingModelTypeNameConverter.to(source.getType()),
            source.getFieldName(),
            source.getModifiers().toArray(new Modifier[0]));

        Collection<AnnotationModel> annotation = source.getAnnotations();
        if (annotation != null) {
            build.addAnnotations(annotation.stream().map(this.annotationModelToAnnotationSpecConverter::to).collect(Collectors.toList()));
        }

        return build.build();
    }

}
