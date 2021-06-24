package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.model.*;
import cn.procsl.ping.processor.utils.NamingUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
public class ControllerTypeBuilder extends TypeModel {

    @NonNull TypeElement typeElement;

    @NonNull String prefix;

    @NonNull ProcessingEnvironment processingEnv;


    final String fieldName = NamingUtils.lowerCamelCase(typeElement.getSimpleName().toString());


    @Override
    public NamingModel getType() {
        return new NamingModel(typeElement.getEnclosingElement().toString(), typeElement.getSimpleName().toString() + "Controller");
    }

    @Override
    public Collection<Modifier> getModifiers() {
        return Collections.singleton(Modifier.PUBLIC);
    }

    @Override

    public Collection<AnnotationModel> getAnnotations() {
        return Arrays.asList(
//            new GeneratedAnnotationBuilder(),
//            new AnnotationModel(new NamingModel("org.springframework.web.bind.annotation", "RestController"))
//            new RequestMappingAnnotationBuilder(prefix, typeElement)
        );
    }


    @Override
    public Collection<FieldModel> getFields() {
        FieldModel field = new FieldModel();
        field.setType(new NamingModel(typeElement.getEnclosingElement().toString(), typeElement.getSimpleName().toString()));
        field.setModifiers(Collections.singleton(Modifier.PUBLIC));
        field.setParent(this);
        field.setName(this.fieldName);
        field.setAnnotations(Collections.singletonList(
            new AnnotationModel(new NamingModel("org.springframework.beans.factory.annotation", "Autowired"))
        ));
        return Collections.singleton(field);
    }

    @Override
    public Collection<MethodModel> getMethods() {
        return typeElement.getEnclosedElements()
            .stream()
            .filter(item -> item instanceof ExecutableElement)
            .map(item -> (ExecutableElement) item)
            .filter(item -> item.getModifiers().contains(Modifier.PUBLIC))
            .map(item -> new SpringMethodModelBuilder(item, fieldName, processingEnv, this))
            .collect(Collectors.toList());
    }


}
