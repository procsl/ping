package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.FieldModel;
import cn.procsl.ping.processor.model.NamingModel;
import cn.procsl.ping.processor.model.ParameterModel;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.stream.Collectors;

@SuperBuilder
class ParameterConverter implements ModelConverter<ParameterModel, ParameterSpec> {

    @NonNull
    private final ModelConverter<NamingModel, TypeName> namingModelTypeNameConverter;

    @NonNull
    private final ModelConverter<AnnotationModel, AnnotationSpec> annotationModelToAnnotationSpecConverter;

    @Override
    public ParameterSpec convertTo(ParameterModel source) {

        ParameterSpec.Builder build = ParameterSpec.builder(
            namingModelTypeNameConverter.convertTo(source.getType()),
            source.getName(),
            source.getModifiers().toArray(new Modifier[0]));

        Collection<AnnotationModel> annotation = source.getAnnotations();
        if (annotation != null) {
            build.addAnnotations(annotation.stream().map(this.annotationModelToAnnotationSpecConverter::convertTo).collect(Collectors.toList()));
        }

        return build.build();
    }

}
