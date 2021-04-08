package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.FieldModel;
import cn.procsl.ping.processor.model.NamingModel;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.stream.Collectors;

@SuperBuilder
public class FieldConverter extends AbstractAwareConvertor<FieldModel, FieldSpec> {

    @NonNull
    private final ModelConverter<NamingModel, TypeName> namingModelTypeNameConverter;

    @NonNull
    private final ModelConverter<AnnotationModel, AnnotationSpec> annotationModelToAnnotationSpecConverter;

    @Override
    protected FieldSpec convertTo(FieldModel source) {
        FieldSpec.Builder fieldBuilder = FieldSpec
            .builder(this.namingModelTypeNameConverter.to(source.getType()),
                source.getFieldName(),
                source.getModifiers().toArray(new Modifier[0]));
        Collection<AnnotationModel> annotation = source.getAnnotations();
        if (annotation != null) {
            fieldBuilder.addAnnotations(annotation.stream().map(this.annotationModelToAnnotationSpecConverter::to).collect(Collectors.toList()));
        }
        return fieldBuilder.build();
    }

}
