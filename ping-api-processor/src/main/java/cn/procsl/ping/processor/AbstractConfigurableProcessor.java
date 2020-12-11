package cn.procsl.ping.processor;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.emptyMap;
import static javax.tools.Diagnostic.Kind.WARNING;

@Slf4j
public abstract class AbstractConfigurableProcessor extends AbstractProcessor {
    protected Messager messager;
    protected Filer filer;
    protected String processor = "META-INF/processor.config";
    protected Map<Object, Object> config;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {

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
        super.init(processingEnv);
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
