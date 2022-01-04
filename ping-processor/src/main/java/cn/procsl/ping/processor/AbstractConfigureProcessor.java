package cn.procsl.ping.processor;

import lombok.Getter;
import lombok.NonNull;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static javax.tools.Diagnostic.Kind.WARNING;

// 明天移除配置获取相关的代码， 移动到Env中
public abstract class AbstractConfigureProcessor extends AbstractProcessor {

    @Getter
    protected Messager messager;

    @Getter
    protected Filer filer;

    protected String processor = "META-INF/processor.config";

    protected Map<Object, Object> config;

    // 应该要加载构建器相关的代码
    protected TypeSpecBuilder builder;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
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
            Collection<TypeElement> collection = this.processor(annotations, roundEnv);
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

    abstract protected Collection<TypeElement> processor(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException;

    @Override
    public final synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        try (InputStream is = processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH, "", processor).openInputStream()) {
            Properties properties = new Properties();
            properties.load(is);
            this.config = properties;
            init();
        } catch (IOException e) {
            this.config = emptyMap();
            this.processingEnv.getMessager().printMessage(WARNING, "The profile could not be found: '" + processor + "'. by error:" + e.getMessage());
        }
    }

    protected void init() {
    }

    /**
     * 获取系统配置
     *
     * @param key 系统配置key
     * @return 返回找到的系统配置
     */
    public String getConfig(String key) {
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

    @NonNull
    @SuppressWarnings("unused")
    protected String getConfig(String key, String defaultValue) {
        String v = this.getConfig(key);
        if (v == null) {
            return defaultValue;
        }
        return v;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
