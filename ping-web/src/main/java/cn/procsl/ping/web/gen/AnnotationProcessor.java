package cn.procsl.ping.web.gen;

import cn.procsl.ping.web.gen.process.GetClassGenerator;
import cn.procsl.ping.web.gen.process.PostClassGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @author procsl
 * @date 2019/10/07
 */
public class AnnotationProcessor extends AbstractProcessor {

    private HashMap<String, ClassGenerator> processMap;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processMap = new HashMap<>(2);

        ClassGenerator post = new PostClassGenerator();
        this.processMap.put(post.support(), post);

        ClassGenerator get = new GetClassGenerator();
        this.processMap.put(get.support(), get);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return this.processMap.keySet();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        if (this.processMap.isEmpty()) {
            return false;
        }

        annotations.iterator().forEachRemaining(element -> {
            if (this.processMap.containsKey(element.asType().toString())) {
                ClassGenerator generator = this.processMap.get(element.asType().toString());
                generator.process(this.processingEnv, roundEnv, element);
            }
        });
        return true;
    }
}
