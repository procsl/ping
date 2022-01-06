package cn.procsl.ping.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.Set;

public abstract class AbstractConfigureProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        ProcessorEnvironment processorEnvironment = new SimpleEnvironment(this.processingEnv, roundEnv);

        if (roundEnv.processingOver()) {
            return false;
        }

        if (!isInitialized()) {
            return false;
        }

        if (annotations.isEmpty()) {
            return true;
        }

        try {
            Collection<TypeElement> collection = this.findTargetElements(annotations, processorEnvironment);
            for (TypeElement element : collection) {
                this.getBuilder().create(element, processorEnvironment);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    abstract protected Collection<TypeElement> findTargetElements(Set<? extends TypeElement> annotations, ProcessorEnvironment env);

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    // 应该要加载构建器相关的代码
    abstract protected TypeSpecBuilder getBuilder();

}
