package cn.procsl.ping.processor.v3;

import lombok.NonNull;

import javax.annotation.Nullable;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.emptyMap;
import static javax.tools.Diagnostic.Kind.WARNING;

public class SimpleEnvironment implements ProcessorEnvironment {

    protected final ProcessingEnvironment processingEnv;
    protected final RoundEnvironment roundEnvironment;
    protected final Messager messager;
    protected final Filer filer;
    protected final Map<Object, Object> config;
    protected String processor = "META-INF/processor.config";

    public SimpleEnvironment(ProcessingEnvironment processorEnvironment, RoundEnvironment roundEnvironment) {
        this.roundEnvironment = roundEnvironment;
        this.processingEnv = processorEnvironment;
        this.messager = processorEnvironment.getMessager();
        this.filer = processorEnvironment.getFiler();

        Map<Object, Object> tmp;
        try (InputStream is = processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH, "", processor).openInputStream()) {
            Properties properties = new Properties();
            properties.load(is);
            tmp = properties;
        } catch (IOException e) {
            tmp = emptyMap();
            this.processingEnv.getMessager().printMessage(WARNING, "The profile could not be found: '" + processor + "'. by error:" + e.getMessage());
        }
        this.config = tmp;
    }


    public RoundEnvironment getRoundEnvironment() {
        return roundEnvironment;
    }

    @Nullable
    @Override
    public TypeElement getTypeElementByName(String elementName) {
        return this.processingEnv.getElementUtils().getTypeElement(elementName);
    }

    @Override
    public ProcessingEnvironment getProcessingEnvironment() {
        return processingEnv;
    }

    @Override
    public Messager getMessager() {
        return messager;
    }

    @Override
    public Filer getFiler() {
        return filer;
    }

    public String getConfig(@NonNull String key) {
        // 首先从config加载
        Object prop = this.config.get(key);
        if (prop == null) {
            return this.processingEnv.getOptions().get(key);
        }

        if (prop instanceof String) {
            return (String) prop;
        }

        if (prop instanceof Number) {
            return String.valueOf(prop);
        }

        messager.printMessage(WARNING, "This property is not a simple type: " + key);
        return null;
    }

    /**
     * 获取指定的类型的包装器
     *
     * @param realTypeName 类型名称
     * @return 返回包装器名称
     */
    @Override
    public String getWrapperType(TypeMirror realTypeName) {
        return null;
    }

    @Override
    public boolean isPublic(ExecutableElement element) {
        return false;
    }

    @Override
    public boolean isStatic(ExecutableElement element) {
        return false;
    }

    @Override
    public AnnotationMirror findAnnotationMirror(Element element, String name) {
        return null;
    }

    @Override
    public AnnotationMirror findAnnotationMirror(Element element, Class<? extends Annotation> name) {
        return null;
    }


}
