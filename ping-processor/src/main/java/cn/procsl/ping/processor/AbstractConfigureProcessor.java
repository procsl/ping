package cn.procsl.ping.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

// 明天移除配置获取相关的代码， 移动到Env中
public abstract class AbstractConfigureProcessor extends AbstractProcessor {

    // 应该要加载构建器相关的代码
    protected TypeSpecBuilder builder;

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
            Collection<TypeElement> collection = this.processor(annotations, processorEnvironment);
            for (TypeElement element : collection) {
                // TODO
                ProcessorEnvironment env = null;
                this.builder.build(element, env);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    abstract protected Collection<TypeElement> processor(Set<? extends TypeElement> annotations, ProcessorEnvironment env) throws IOException;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
