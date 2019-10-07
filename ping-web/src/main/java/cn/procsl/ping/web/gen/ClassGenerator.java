package cn.procsl.ping.web.gen;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * @author procsl
 * @date 2019/10/07
 */
public interface ClassGenerator {

    /**
     * 支持处理的注解
     *
     * @return
     */
    String support();

    /**
     * 处理器
     *
     * @param processingEnv
     * @param roundEnv
     * @param element
     * @return
     */
    void process(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv, TypeElement element);
}
