package cn.procsl.ping.processor.api;

import cn.procsl.ping.processor.api.utils.NamingUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import lombok.Getter;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@AutoService(Processor.class)
public class GeneratorProcessor extends AbstractConfigurableProcessor implements ProcessorContext {

    final static String CONTROLLER = "CONTROLLER";

    final static String SERVICE = "Service";

    final List<GeneratorBuilder> controller = new ArrayList<>();

    final List<GeneratorBuilder> dto = new ArrayList<>();

    @Getter
    RoundEnvironment roundEnvironment;

    @Getter
    Set<? extends TypeElement> annotations;

    @Override
    protected void init() {
        final ServiceLoader<GeneratorBuilder> generator = ServiceLoader.load(GeneratorBuilder.class, this.getClass().getClassLoader());
        for (GeneratorBuilder builder : generator) {
            builder.init(this);
            if (builder.support(CONTROLLER)) {
                controller.add(builder);
            }
            if (builder.support("DTO")) {
                dto.add(builder);
            }
        }
    }

    @Override
    protected void processor(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        this.roundEnvironment = roundEnv;
        this.annotations = annotations;

        Set<TypeElement> element = this.selector(roundEnv);
        for (TypeElement typeElement : element) {

            String name = this.createClassName(typeElement);

            TypeSpec.Builder builder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);

            for (GeneratorBuilder specBuilder : controller) {
                specBuilder.typeAnnotation(CONTROLLER, typeElement, builder);
            }

            String fieldName = NamingUtils.lowerCamelCase(typeElement.getSimpleName().toString());
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(TypeName.get(typeElement.asType()), fieldName, Modifier.PROTECTED);
            for (GeneratorBuilder specBuilder : controller) {
                specBuilder.fieldAnnotation(CONTROLLER, typeElement, fieldSpecBuilder);
            }
            builder.addField(fieldSpecBuilder.build());

            FieldSpec.Builder request = FieldSpec.builder(ClassName.get("javax.servlet.http", "HttpServletRequest"), "request", Modifier.PROTECTED);
            for (GeneratorBuilder specBuilder : controller) {
                specBuilder.fieldAnnotation(CONTROLLER, typeElement, request);
            }
            builder.addField(request.build());

            List<ExecutableElement> list = typeElement.getEnclosedElements().stream()
                .filter(item -> item instanceof ExecutableElement)
                .filter(this::methodSelector)
                .map(item -> (ExecutableElement) item)
                .collect(Collectors.toList());
            for (ExecutableElement item : list) {
                this.buildMethod(builder, item, fieldName);
            }


            String packageName = typeElement.getEnclosingElement().toString() + ".api";
            JavaFile java = JavaFile
                .builder(packageName, builder.build())
                .addFileComment("这是自动生成的代码，请勿修改").build();
            java.writeTo(this.getFiler());
        }
    }

    boolean methodSelector(Element element) {
        Set<String> set = element.getAnnotationMirrors()
            .stream()
            .map(item -> item.getAnnotationType().asElement().toString())
            .collect(Collectors.toSet());
        return set.contains("javax.ws.rs.PATCH")
            || set.contains("javax.ws.rs.POST")
            || set.contains("javax.ws.rs.GET")
            || set.contains("javax.ws.rs.DELETE")
            || set.contains("javax.ws.rs.PATH")
            || set.contains("javax.ws.rs.PUT");
    }

    private void buildMethod(TypeSpec.Builder builder, ExecutableElement item, String fieldName) throws IOException {
        String methodName = item.getSimpleName().toString();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC);

        for (TypeMirror typeMirror : item.getThrownTypes()) {
            methodBuilder.addException(TypeName.get(typeMirror));
        }

        for (GeneratorBuilder specBuilder : controller) {
            specBuilder.methodAnnotation(CONTROLLER, item, methodBuilder);
        }

        ParameterBuilder constructor = new ParameterBuilder(this, methodName, fieldName, item);
        methodBuilder.addParameters(constructor.buildParameters());

        for (GeneratorBuilder generatorBuilder : controller) {
            TypeName returnType = generatorBuilder.returnType(CONTROLLER, item);
            if (returnType != null) {
                methodBuilder.returns(returnType);
            }
            CodeBlock codeBlack = generatorBuilder.returnCodeBlack(CONTROLLER, item, constructor.buildCaller());
            if (codeBlack != null) {
                methodBuilder.addCode(codeBlack);
            }
        }
        builder.addMethod(methodBuilder.build());
    }


    @Override
    public ProcessingEnvironment getProcessingEnvironment() {
        return this.processingEnv;
    }

    @Override
    public Messager getMessager() {
        return this.messager;
    }

    @Override
    public Filer getFiler() {
        return filer;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Path.class.getName());
    }

    Set<TypeElement> selector(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Path.class);
        return elements
            .stream()
            .filter(item -> item instanceof ExecutableElement)
            .map(item -> (ExecutableElement) item)
            .map(Element::getEnclosingElement)
            .filter(item -> item instanceof TypeElement)
            .map(item -> (TypeElement) item)
            .collect(Collectors.toSet());
    }

    String createClassName(TypeElement element) {
        String name = element.getSimpleName().toString();
        if (name.endsWith(SERVICE)) {
            name = name.replaceAll("Application", "").replaceAll(SERVICE + "$", "Controller");
        } else {
            name = name + "Controller";
        }
        return name;
    }


}
