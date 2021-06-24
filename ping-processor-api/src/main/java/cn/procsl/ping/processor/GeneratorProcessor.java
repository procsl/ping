package cn.procsl.ping.processor;

import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.processing.*;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

@AutoService(Processor.class)
public class GeneratorProcessor extends AbstractConfigurableProcessor implements GeneratorContext {

    final HashMap<String, Object> attrs = new HashMap<>();

    ServiceLoader<CodeGenerator> loader;

    @Getter
    RoundEnvironment roundEnvironment;

    @Getter
    Set<? extends TypeElement> annotations;

    @Override
    protected void init() {
        loader = ServiceLoader.load(CodeGenerator.class);
    }

    @Override
    protected void processor(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        synchronized (attrs) {
            this.roundEnvironment = roundEnv;
            this.annotations = annotations;
        }
        for (CodeGenerator generator : loader) {
            generator.generate(this);
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
        HashSet<String> set = new HashSet<>();
        for (CodeGenerator generator : this.loader) {
            set.add(generator.getSupportAnnotation());
        }
        return set;
    }


}
