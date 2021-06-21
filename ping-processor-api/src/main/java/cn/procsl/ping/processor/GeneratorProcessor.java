package cn.procsl.ping.processor;

import lombok.Getter;
import lombok.NonNull;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.ServiceLoader;
import java.util.Set;

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
    public synchronized void setAttribute(@NonNull String key, Object attr) {
        this.attrs.put(key, attr);
    }

    @Override
    public synchronized Object getAttribute(@NonNull String key) {
        return this.attrs.get(key);
    }
}
