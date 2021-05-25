package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.FieldModel;
import cn.procsl.ping.processor.model.NamingModel;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.stream.Collectors;

@SuperBuilder
class FieldConverter implements ModelConverter<FieldModel, FieldSpec> {

    @NonNull
    private final ModelConverter<AnnotationModel, AnnotationSpec> annotationModelToAnnotationSpecConverter;

    @Override
    public FieldSpec convertTo(FieldModel source) {
        TypeName typeName = ClassName.get(source.getType().getPackageName(), source.getType().getTypeName());

        FieldSpec.Builder fieldBuilder = FieldSpec
            .builder(typeName,
                source.getName(),
                source.getModifiers().toArray(new Modifier[0]));
        Collection<AnnotationModel> annotation = source.getAnnotations();
        if (annotation != null) {
            fieldBuilder.addAnnotations(annotation.stream().map(this.annotationModelToAnnotationSpecConverter::convertTo).collect(Collectors.toList()));
        }
        return fieldBuilder.build();
    }

}
