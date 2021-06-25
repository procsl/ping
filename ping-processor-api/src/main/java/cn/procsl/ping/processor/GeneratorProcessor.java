package cn.procsl.ping.processor;

import cn.procsl.ping.processor.generator.TypeAnnotationSpecBuilder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class GeneratorProcessor extends AbstractConfigurableProcessor implements ProcessorContext {

    final HashMap<String, Object> attrs = new HashMap<>();

    final ServiceLoader<TypeAnnotationSpecBuilder> typeAnnotationSpecBuilders = ServiceLoader.load(TypeAnnotationSpecBuilder.class, this.getClass().getClassLoader());

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

            for (TypeAnnotationSpecBuilder specBuilder : typeAnnotationSpecBuilders) {
                AnnotationSpec spec = specBuilder.build(this, typeElement);
                if (spec != null) {
                    builder.addAnnotation(spec);
                }
            }

            JavaFile java = JavaFile
                .builder("cn.procsl.ping.gen.web.rest", builder.build())
                .addFileComment("这是自动生成的代码， 请勿修改").build();
            java.writeTo(this.getFiler());
        }
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
        if (name.endsWith("Service")) {
            name = name.replaceAll("Service$", "Controller");
        } else {
            name = name + "Controller";
        }
        return name;
    }


}
