package cn.procsl.ping.processor;

import cn.procsl.ping.processor.converter.*;
import cn.procsl.ping.processor.model.TypeModel;
import cn.procsl.ping.processor.spring.SpringTypeModel;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes({"javax.ws.rs.Path"})
@Slf4j
@AutoService(Processor.class)
public class SpringMVCProcessor extends AbstractConfigurableProcessor {

    @Override
    protected void processor(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Path.class);
        List<TypeElement> pathElements = elements.stream().filter(item -> item instanceof ExecutableElement)
            .map(item -> (ExecutableElement) item)
            .map(Element::getEnclosingElement)
            .filter(item -> item instanceof TypeElement)
            .map(item -> (TypeElement) item)
            .distinct()
            .collect(Collectors.toList());

        ModelConverter<TypeModel, JavaFile> sourceConverter = buildConvert(roundEnv);

        for (TypeElement pathElement : pathElements) {
            SpringTypeModel model = new SpringTypeModel(pathElement);
            JavaFile source = sourceConverter.to(model);
            source.writeTo(filer);
        }

    }

    public SourceConverter buildConvert(RoundEnvironment roundEnvironment) {
        AnnotationConverter annotationConvert = AnnotationConverter
            .builder()
            .processingEnv(this.processingEnv)
            .roundEnv(roundEnvironment)
            .codeBlockModelConverter(CodeModelConverter.INSTANCE)
            .namingModelClassNameConverter(NamingToClassConverter.INSTANCE)
            .build();

        ParameterConverter parameterConvert = ParameterConverter.builder()
            .processingEnv(this.processingEnv)
            .roundEnv(roundEnvironment)
            .annotationModelToAnnotationSpecConverter(annotationConvert)
            .namingModelTypeNameConverter(NamingToTypeConverter.INSTANCE)
            .build();

        MethodConverter methodConverter = MethodConverter.builder()
            .processingEnv(this.processingEnv)
            .roundEnv(roundEnvironment)
            .annotationModelToAnnotationSpecConverter(annotationConvert)
            .fieldModelParameterSpecConverter(parameterConvert)
            .codeBlockModelConverter(CodeModelConverter.INSTANCE)
            .namingModelTypeNameConverter(NamingToTypeConverter.INSTANCE)
            .build();

        FieldConverter fieldConverter = FieldConverter.builder()
            .processingEnv(this.processingEnv)
            .roundEnv(roundEnvironment)
            .annotationModelToAnnotationSpecConverter(annotationConvert)
            .namingModelTypeNameConverter(NamingToTypeConverter.INSTANCE)
            .build();

        TypeConverter typeConvert = TypeConverter.builder()
            .processingEnv(this.processingEnv)
            .roundEnv(roundEnvironment)
            .codeBlockModelConverter(CodeModelConverter.INSTANCE)
            .annotationModelToAnnotationSpecConverter(annotationConvert)
            .methodModelToMethodSpecConverter(methodConverter)
            .fieldModelToFieldSpecConverter(fieldConverter)
            .variableNamingModelTypeNameConverter(VariableTypeConverter.INSTANCE)
            .build();

        return SourceConverter.builder()
            .typeModelTypeSpecModelConverter(typeConvert)
            .build();
    }


}
