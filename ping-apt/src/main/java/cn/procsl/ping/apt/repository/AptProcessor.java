package cn.procsl.ping.apt.repository;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.function.Consumer;

@AutoService(Processor.class)
public class AptProcessor extends AbstractProcessor {

    final ComposeProcessor processor = new ComposeProcessor();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processor.addBuilder(new RepositoryCreatorVisitor());
        processor.addBuilder(new RepositoryCreatorsVisitor());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return processor.getSupportedAnnotationTypes();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        for (TypeElement target : annotations) {
            Consumer<Element> item = (e) -> this.processor.build(this.processingEnv, roundEnv, target, e);
            roundEnv.getElementsAnnotatedWith(target).forEach(item);
        }

        return true;
    }

}
