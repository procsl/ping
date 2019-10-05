package cn.procsl.ping.util;

import lombok.Cleanup;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_SET;

/**
 * 注解处理器
 *
 * @author procsl
 * @date 2019/09/29
 */
public class AnnotationProcess extends AbstractProcessor {


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        annotations.forEach(e -> {
            Set<? extends Element> clazz = roundEnv.getElementsAnnotatedWith(e);
        });

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Properties properties = new Properties();
        try {
            @Cleanup
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("Annotation.properties");

            properties.load(is);
            String string = properties.getProperty("ping.scan.list");
            if (string == null || string.isEmpty()) {
                System.err.println("未找到配置项");
                return EMPTY_SET;
            }
            String[] array = string.split(",");
            return new HashSet<>(asList(array));
        } catch (IOException e) {
            System.err.println("配置文件读取失败!");
        }
        return EMPTY_SET;
    }
}
