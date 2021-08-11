package cn.procsl.ping.processor.api;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractGeneratorProcessor extends AbstractConfigurableProcessor {

    private List<SourceCodeBuilder> builder;

    @Override
    protected void init() {
        final ServiceLoader<SourceCodeBuilder> generator = ServiceLoader.load(SourceCodeBuilder.class, this.getClass().getClassLoader());
        this.builder = generator
            .stream()
            .map(ServiceLoader.Provider::get)
            .filter(Objects::nonNull)
            .peek(item -> item.init(this.processingEnv))
            .filter(item -> item.isSupport(this.generatedType()))
            .sorted(Comparator.comparingInt(SourceCodeBuilder::order))
            .collect(Collectors.toList());
    }

    @Override
    protected void processor(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        for (TypeElement annotation : annotations) {
            TypeSpec type = this.generated(annotation, roundEnv);
            if (type == null) {
                continue;
            }

            for (SourceCodeBuilder sourceCodeBuilder : this.builder) {
                type = sourceCodeBuilder.toBuilder(this.getPackage(annotation), annotation, type, roundEnv);
            }
            this.write(annotation, type);
        }
    }

    protected void write(TypeElement annotation, TypeSpec type) {
        try {
            JavaFile.builder(this.getPackage(annotation), type).build().writeTo(this.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
            this.messager.printMessage(Diagnostic.Kind.WARNING, e.getMessage());
        }
    }

    protected abstract TypeSpec generated(TypeElement annotation, RoundEnvironment roundEnv);

    protected abstract String getPackage(TypeElement annotation);

    protected abstract String generatedType();
}
