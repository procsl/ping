package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.*;
import com.squareup.javapoet.*;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuperBuilder
class MethodConverter implements ModelConverter<MethodModel, MethodSpec> {

    @NonNull
    private final ModelConverter<AnnotationModel, AnnotationSpec> annotationModelToAnnotationSpecConverter;

    @NonNull
    private final ModelConverter<NamingModel, TypeName> namingModelTypeNameConverter;

    @NonNull
    private final ModelConverter<ParameterModel, ParameterSpec> parameterSpecModelConverter;

    @NonNull
    private final ModelConverter<String, CodeBlock> codeBlockModelConverter;


    @Override
    public MethodSpec convertTo(MethodModel source) {

        MethodSpec.Builder builder = MethodSpec.methodBuilder(source.getName());

        Collection<AnnotationModel> annotation = source.getAnnotations();
        if (annotation != null) {
            builder.addAnnotations(annotation.stream().map(this.annotationModelToAnnotationSpecConverter::convertTo).collect(Collectors.toList()));
        }

        NamingModel returned = source.getReturned();
        if (returned != null) {
            builder.returns(namingModelTypeNameConverter.convertTo(returned));
        }

        Collection<NamingModel> throwAbles = source.getThrowable();
        if (throwAbles != null) {
            List<TypeName> list = throwAbles.stream().map(this.namingModelTypeNameConverter::convertTo).collect(Collectors.toList());
            builder.addExceptions(list);
        }

        builder.addModifiers(source.getModifiers().toArray(new Modifier[0]));
        List<ParameterModel> parameters = source.getParameters();
        if (parameters != null) {
            builder.addParameters(parameters.stream().map(this.parameterSpecModelConverter::convertTo).collect(Collectors.toList()));
        }

        String code = source.getBody();
        if (code != null) {
            CodeBlock black = this.codeBlockModelConverter.convertTo(code);
            builder.addCode(black);
        }

        return builder.build();
    }

}
