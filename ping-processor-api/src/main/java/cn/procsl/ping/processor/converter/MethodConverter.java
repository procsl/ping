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
public class MethodConverter extends AbstractAwareConvertor<MethodModel, MethodSpec> {

    @NonNull
    private final ModelConverter<AnnotationModel, AnnotationSpec> annotationModelToAnnotationSpecConverter;

    @NonNull
    private final ModelConverter<NamingModel, TypeName> namingModelTypeNameConverter;

    @NonNull
    private final ModelConverter<FieldModel, ParameterSpec> fieldModelParameterSpecConverter;

    @NonNull
    private final ModelConverter<CodeModel, CodeBlock> codeBlockModelConverter;


    @Override
    protected MethodSpec convertTo(MethodModel source) {

        MethodSpec.Builder builder = MethodSpec.methodBuilder(source.getMethodName());

        Collection<AnnotationModel> annotation = source.getAnnotations();
        if (annotation != null) {
            builder.addAnnotations(annotation.stream().map(this.annotationModelToAnnotationSpecConverter::to).collect(Collectors.toList()));
        }

        NamingModel returned = source.getReturn();
        if (returned != null) {
            builder.returns(namingModelTypeNameConverter.to(returned));
        }

        Collection<NamingModel> throwAbles = source.getThrowable();
        if (throwAbles != null) {
            List<TypeName> list = throwAbles.stream().map(this.namingModelTypeNameConverter::to).collect(Collectors.toList());
            builder.addExceptions(list);
        }

        builder.addModifiers(source.getModifiers().toArray(new Modifier[0]));
        Collection<FieldModel> parameters = source.getArguments();
        if (parameters != null) {
            builder.addParameters(parameters.stream().map(this.fieldModelParameterSpecConverter::to).collect(Collectors.toList()));
        }

        CodeModel code = source.getBody();
        if (code != null) {
            CodeBlock black = this.codeBlockModelConverter.to(code);
            builder.addCode(black);
        }

        return builder.build();
    }

}
