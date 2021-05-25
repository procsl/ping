package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.TypeModel;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.NonNull;

public class SourceConverter implements ModelConverter<TypeModel, JavaFile> {

    final AnnotationConverter annotationConverter = AnnotationConverter.builder().build();

    final FieldConverter fieldConverter = FieldConverter.builder()
        .annotationModelToAnnotationSpecConverter(annotationConverter)
        .build();

    final MethodConverter methodConverter = MethodConverter.builder()
        .annotationModelToAnnotationSpecConverter(annotationConverter)
        .parameterSpecModelConverter(ParameterConverter.builder()
            .annotationModelToAnnotationSpecConverter(annotationConverter)
            .build())
        .build();

    final ModelConverter<TypeModel, TypeSpec> typeModelTypeSpecModelConverter = TypeConverter.builder()
        .annotationModelToAnnotationSpecConverter(annotationConverter)
        .fieldModelToFieldSpecConverter(fieldConverter)
        .methodModelToMethodSpecConverter(methodConverter)
        .build();

    @Override
    public JavaFile convertTo(@NonNull TypeModel source) {
        return JavaFile.builder(source.getType().getPackageName(), typeModelTypeSpecModelConverter.convertTo(source)).build();
    }
}
