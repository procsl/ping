package cn.procsl.ping.meta.process;


import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.Set;

import static java.util.Collections.EMPTY_SET;

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
    public Set<String> getSupportedAnnotationTypes() {
        messager.printMessage(Diagnostic.Kind.NOTE, "getSupportedAnnotationTypes");
//        return Collections.singleton(Extends.class.getName());
        return EMPTY_SET;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        messager.printMessage(Diagnostic.Kind.NOTE, "getSupportedSourceVersion");
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return true;
    }

}
