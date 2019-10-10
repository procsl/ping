package cn.procsl.ping.web.gen;

import cn.procsl.ping.web.gen.process.GetClassGenerator;
import cn.procsl.ping.web.gen.process.PostClassGenerator;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacFiler;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;

/**
 * @author procsl
 * @date 2019/10/07
 */
public class AnnotationProcessor extends AbstractProcessor {

    private JavacProcessingEnvironment javacProcessingEnv;
    private JavacFiler javacFiler;
    private Trees trees;
    private Messager messager;
    private HashMap<String, ClassGenerator> processMap;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {

        super.init(processingEnv);
        this.messager = processingEnv.getMessager();

        this.javacProcessingEnv = getJavacProcessingEnvironment(processingEnv);
        this.javacFiler = getJavacFiler(processingEnv.getFiler());

        this.messager.printMessage(Diagnostic.Kind.NOTE, "注解处理器初始化成功");

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
            this.messager.printMessage(Diagnostic.Kind.NOTE, "注解处理成功");
            return false;
        }

        if (this.processMap.isEmpty()) {
            this.messager.printMessage(Diagnostic.Kind.NOTE, "待处理的注解为空,忽略");
            return false;
        }

        this.messager.printMessage(Diagnostic.Kind.NOTE, "开始处理注解");
        annotations.iterator().forEachRemaining(element -> {
            if (this.processMap.containsKey(element.asType().toString())) {
                ClassGenerator generator = this.processMap.get(element.asType().toString());
                generator.process(this.processingEnv, roundEnv, element);
            }
        });
        return true;
    }

    public JavacFiler getJavacFiler(Object filer) {
        if (filer instanceof JavacFiler) {
            return (JavacFiler) filer;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "无法获取:JavacFiler");
        return null;
    }


    public static Field getField(Class<?> c, String fName) throws NoSuchFieldException {
        Field f = null;
        Class<?> oc = c;
        while (c != null) {
            try {
                f = c.getDeclaredField(fName);
                break;
            } catch (NoSuchFieldException e) {}
            c = c.getSuperclass();
        }

        if (f == null) {
            throw new NoSuchFieldException(oc.getName() + " :: " + fName);
        }

        return setAccessible(f);
    }

    public static <T extends AccessibleObject> T setAccessible(T accessor) {
        accessor.setAccessible(true);
        return accessor;
    }


    public JavacProcessingEnvironment getJavacProcessingEnvironment(Object procEnv) {
        if (procEnv instanceof JavacProcessingEnvironment) {
            return (JavacProcessingEnvironment) procEnv;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "无法获取:JavacProcessingEnvironment");
        return null;
    }


}
