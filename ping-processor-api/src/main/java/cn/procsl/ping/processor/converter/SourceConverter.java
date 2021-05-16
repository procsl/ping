package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.TypeModel;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.NonNull;

public class SourceConverter implements ModelConverter<TypeModel, JavaFile> {

    final AnnotationConverter annotationConverter = AnnotationConverter.builder()
        .codeBlockModelConverter(CodeBlockModelConverter.INSTANCE)
        .namingModelClassNameConverter(NamingModeConverter.INSTANCE)
        .build();

    final FieldConverter fieldConverter = FieldConverter.builder()
        .annotationModelToAnnotationSpecConverter(annotationConverter)
        .namingModelTypeNameConverter(NamingModeConverter.INSTANCE)
        .build();

    final MethodConverter methodConverter = MethodConverter.builder()
        .annotationModelToAnnotationSpecConverter(annotationConverter)
        .codeBlockModelConverter(CodeBlockModelConverter.INSTANCE)
        .parameterSpecModelConverter(ParameterConverter.builder()
            .annotationModelToAnnotationSpecConverter(annotationConverter)
            .namingModelTypeNameConverter(NamingModeConverter.INSTANCE)
            .build())
        .namingModelTypeNameConverter(NamingModeConverter.INSTANCE)
        .build();

    final ModelConverter<TypeModel, TypeSpec> typeModelTypeSpecModelConverter = TypeConverter.builder()
        .annotationModelToAnnotationSpecConverter(annotationConverter)
        .nameModelConverter(NamingModeConverter.INSTANCE)
        .codeBlockModelConverter(CodeBlockModelConverter.INSTANCE)
        .fieldModelToFieldSpecConverter(fieldConverter)
        .methodModelToMethodSpecConverter(methodConverter)
        .build();

    @Override
    public JavaFile convertTo(@NonNull TypeModel source) {
        return JavaFile.builder(source.getType().getPackageName(), typeModelTypeSpecModelConverter.convertTo(source)).build();
    }
}
