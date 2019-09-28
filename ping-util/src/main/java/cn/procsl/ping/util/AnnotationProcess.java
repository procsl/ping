package cn.procsl.ping.util;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * 注解处理器
 *
 * @author procsl
 * @date 2019/09/29
 */
@Slf4j
public class AnnotationProcess extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        log.debug("开始处理注解");
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        log.debug("获取待处理的注解的类型");
        return super.getSupportedAnnotationTypes();
    }
}
