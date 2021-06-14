package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.model.*;
import cn.procsl.ping.processor.utils.NamingUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.*;
import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
public class ControllerTypeBuilder extends TypeModel {

    @NonNull TypeElement typeElement;

    @NonNull String prefix;

    @NonNull ProcessingEnvironment processingEnv;

    final String fieldName;

    {
        fieldName = NamingUtils.lowerCamelCase(typeElement.getSimpleName().toString());
    }


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
            new GeneratedAnnotationBuilder(),
            new AnnotationModel(new NamingModel("org.springframework.web.bind.annotation", "RestController")),
            new RequestMappingAnnotationBuilder(prefix, typeElement)
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
            .map(item -> {
                MethodModel method = new MethodModel();
                method.setModifiers(Collections.singleton(Modifier.PUBLIC));
                method.setName(item.getSimpleName().toString());
                method.setParent(ControllerTypeBuilder.this);
                TypeMirror type = item.getReturnType();
                Element element = processingEnv.getTypeUtils().asElement(type);
                method.setReturned(new NamingModel(element.getEnclosingElement().toString(), element.getSimpleName().toString()));

                ParameterResolver resolver = new ParameterResolver(this.fieldName, item, method);
                method.setParameters(resolver.resolver());
                method.setAnnotations(Arrays.asList(new RequestMappingAnnotationBuilder(item)));
                method.setBody(
                    String.format("return %s; ", resolver.getCaller())
                );
                return method;
            })
            .collect(Collectors.toList());
    }

    @RequiredArgsConstructor
    private static final class ParameterResolver {

        @NonNull String fieldName;

        @NonNull ExecutableElement item;

        @NonNull MethodModel methodModel;

        public List<ParameterModel> resolver() {
            POST post = this.item.getAnnotation(POST.class);
            PATCH patch = this.item.getAnnotation(PATCH.class);
            PUT put = this.item.getAnnotation(PUT.class);
            if (post == null || patch == null || put == null) {
                return item.getParameters().stream().map(
                    param -> {
                        ParameterModel parameter = new ParameterModel();
                        parameter.setParent(methodModel);
                        parameter.setType(new NamingModel(param.getEnclosingElement().toString(), param.getSimpleName().toString()));
                        parameter.setName(param.getSimpleName().toString());
                        parameter.setModifiers(Collections.singleton(Modifier.FINAL));
                        AnnotationResolver resolver = new AnnotationResolver(item);
                        parameter.setAnnotations(resolver.resolve(param));
                        return parameter;
                    }
                ).collect(Collectors.toList());
            }
            return null;
        }

        public String getCaller() {
            return null;
        }

    }

    @RequiredArgsConstructor
    private static final class AnnotationResolver {

        @NonNull ExecutableElement item;
        boolean simple = (item.getAnnotation(GET.class) == null || item.getAnnotation(DELETE.class) == null);


        public Collection<AnnotationModel> resolve(VariableElement param) {
            List<AnnotationModel> models;
            if (simple) {
                models = Arrays.asList(new RequestParamAnnotationModel(param));
            } else {
                models = Arrays.asList();
            }
            return models;
        }
    }

}
