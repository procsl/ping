package cn.procsl.ping.processor.v2.web;

import cn.procsl.ping.processor.AbstractConfigureProcessor;
import cn.procsl.ping.processor.ProcessorEnvironment;
import cn.procsl.ping.processor.TypeSpecBuilder;
import com.google.auto.service.AutoService;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.lang.model.element.TypeElement;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * 生成 SpringController 类
 */
@AutoService(Processor.class)
@SuppressWarnings("unused")
public class SpringControllerProcessor extends AbstractConfigureProcessor {

    TypeSpecBuilder builder;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.builder = new SpringTypeBuilder(processingEnv);
        super.init(processingEnv);
    }

    /**
     * 获取命中的目标类型， 用于生成
     *
     * @param annotations 被 @Path 标记的注解
     * @param env         获取环境相关参数
     * @return 返回需要生成的基准类
     * @throws IOException 如果出现错误
     */
    @Override
    protected Collection<TypeElement> findTargetElements(Set<? extends TypeElement> annotations, ProcessorEnvironment env) {
        return null;
    }

    @Override
    protected TypeSpecBuilder getBuilder() {
        return builder;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Path.class.getName());
    }
}
