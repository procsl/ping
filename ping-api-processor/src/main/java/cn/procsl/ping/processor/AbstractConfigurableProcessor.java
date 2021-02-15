package cn.procsl.ping.processor;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static javax.tools.Diagnostic.Kind.WARNING;

@Slf4j
public abstract class AbstractConfigurableProcessor extends AbstractProcessor {
    protected Messager messager;
    protected Filer filer;
    protected String processor = "META-INF/processor.config";
    protected Map<Object, Object> config;
    private Collection<String> env;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        if (!isInitialized()) {
            return false;
        }

        try {
//            TODO
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            log.error("编译错误", e);
            return false;
        }

        return true;
    }

    @Override
    public final synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        try (InputStream is = processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH, "", processor).openInputStream()) {
            Properties properties = new Properties();
            properties.load(is);
            this.config = properties;
        } catch (IOException e) {
            this.processingEnv.getMessager().printMessage(WARNING, "The profile could not be found: '" + processor + "'. by error:" + e.getMessage());
            log.warn("The profile could not be found: '{}'. by error:", processor, e);
        }
        this.config = emptyMap();
        String envs = this.getConfig("processor.creator.env", "");
        this.env = Arrays.stream(envs.split(",")).filter(item -> !item.isEmpty()).collect(Collectors.toUnmodifiableSet());
        init();
    }

    protected void init() {
    }

    /**
     * 获取系统配置
     *
     * @param key 系统配置key
     * @return 返回找到的系统配置
     */
    protected String getConfig(String key) {
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
        log.warn("This property is not a simple type: {}", key);
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
