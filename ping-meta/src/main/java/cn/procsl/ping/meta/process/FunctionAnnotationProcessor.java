package cn.procsl.ping.meta.process;

import cn.procsl.ping.meta.annotation.Extends;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.Set;

/**
 * 注解功能处理器
 *
 * @author procsl
 * @date 2020/05/16
 */
public class FunctionAnnotationProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Messager messager;
    private Filer filer;

    @Override
    public Set<String> getSupportedOptions() {
        messager.printMessage(Diagnostic.Kind.NOTE, "getSupportedOptions");
        return super.getSupportedOptions();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        messager.printMessage(Diagnostic.Kind.NOTE, "getSupportedAnnotationTypes");
        return Collections.singleton(Extends.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        messager.printMessage(Diagnostic.Kind.NOTE, "getSupportedSourceVersion");
        return SourceVersion.latestSupported();
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        messager.printMessage(Diagnostic.Kind.NOTE, "getCompletions");
        return super.getCompletions(element, annotation, member, userText);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        messager.printMessage(Diagnostic.Kind.NOTE, "init");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "process");
        return true;
    }

}
