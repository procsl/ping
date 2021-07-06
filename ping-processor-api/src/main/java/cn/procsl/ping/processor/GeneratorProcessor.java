package cn.procsl.ping.processor;

import cn.procsl.ping.processor.builder.AnnotationSpecBuilder;
import cn.procsl.ping.processor.utils.NamingUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import lombok.Getter;
import lombok.NonNull;

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

    final HashMap<String, Object> attrs = new HashMap<>();

    static final String CONTROLLER = "CONTROLLER";


    static final String SERVICE = "Service";

    final ServiceLoader<AnnotationSpecBuilder> annotationSpecBuilders = ServiceLoader.load(AnnotationSpecBuilder.class, this.getClass().getClassLoader());

    @Getter
    RoundEnvironment roundEnvironment;

    @Getter
    Set<? extends TypeElement> annotations;


    @Override
    protected void processor(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        synchronized (attrs) {
            this.roundEnvironment = roundEnv;
            this.annotations = annotations;
        }

        Set<TypeElement> element = this.selector(roundEnv);
        for (TypeElement typeElement : element) {

            String name = this.createClassName(typeElement);

            TypeSpec.Builder builder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);

            for (AnnotationSpecBuilder specBuilder : annotationSpecBuilders) {
                specBuilder.build(this, typeElement, builder, CONTROLLER);
            }

            String fieldName = NamingUtils.lowerCamelCase(typeElement.getSimpleName().toString());
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(TypeName.get(typeElement.asType()), fieldName, Modifier.PROTECTED);
            for (AnnotationSpecBuilder specBuilder : annotationSpecBuilders) {
                specBuilder.build(this, typeElement, fieldSpecBuilder, CONTROLLER);
            }
            builder.addField(fieldSpecBuilder.build());

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
        methodBuilder.returns(TypeName.get(item.getReturnType()));

        for (TypeMirror typeMirror : item.getThrownTypes()) {
            methodBuilder.addException(TypeName.get(typeMirror));
        }

        for (AnnotationSpecBuilder specBuilder : annotationSpecBuilders) {
            specBuilder.build(this, item, methodBuilder, CONTROLLER);
        }

        ParameterConstructor constructor = new ParameterConstructor(this, methodName, fieldName, item);
        methodBuilder.addParameters(constructor.buildParameters());

        methodBuilder.addCode(constructor.buildCaller());
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
    public synchronized void setAttribute(@NonNull String key, Object attr) {
        this.attrs.put(key, attr);
    }

    @Override
    public synchronized Object getAttribute(@NonNull String key) {
        return this.attrs.get(key);
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
