package cn.procsl.ping.web.gen;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacFiler;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;

/**
 * @author procsl
 * @date 2019/10/07
 */
public abstract class AnnotationProcessor extends AbstractProcessor {

    protected String PACKAGE_ITEM_NAME = "gen";

    protected JavacProcessingEnvironment javacProcessingEnv;
    protected JavacFiler javacFiler;
    protected Trees trees;
    protected Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {

        super.init(processingEnv);
        this.messager = processingEnv.getMessager();

        this.javacProcessingEnv = getJavacProcessingEnvironment(processingEnv);
        this.javacFiler = getJavacFiler(processingEnv.getFiler());

        this.messager.printMessage(Diagnostic.Kind.NOTE, "注解处理器初始化成功");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    public JavacFiler getJavacFiler(Object filer) {
        if (filer instanceof JavacFiler) {
            return (JavacFiler) filer;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "无法获取:JavacFiler");
        return null;
    }

    public JavacProcessingEnvironment getJavacProcessingEnvironment(Object procEnv) {
        if (procEnv instanceof JavacProcessingEnvironment) {
            return (JavacProcessingEnvironment) procEnv;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "无法获取:JavacProcessingEnvironment");
        return null;
    }


}
