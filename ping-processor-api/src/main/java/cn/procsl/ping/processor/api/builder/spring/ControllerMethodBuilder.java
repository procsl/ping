package cn.procsl.ping.processor.api.builder.spring;

import cn.procsl.ping.processor.api.Generator;
import cn.procsl.ping.processor.api.ProcessorContext;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.ExecutableElement;

public class ControllerMethodBuilder implements Generator<MethodSpec> {

    final ProcessorContext context;

    final ExecutableElement element;

    final MethodSpec.Builder builder;

    public ControllerMethodBuilder(ExecutableElement element, ProcessorContext context) {
        this.element = element;
        this.context = context;
        this.builder = MethodSpec.methodBuilder(this.getName());
    }

    @Override
    public MethodSpec getSpec() {
        return null;
    }

    String getName() {
        return element.getSimpleName().toString();
    }

}
