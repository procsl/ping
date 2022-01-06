package cn.procsl.ping.processor.v2.web;

import cn.procsl.ping.processor.ProcessorEnvironment;
import cn.procsl.ping.processor.v2.SpecHandler;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Element;

public class GetMethodHandler implements SpecHandler<MethodSpec.Builder> {
    @Override
    public void handle(Element element, MethodSpec.Builder builder, ProcessorEnvironment environment) {

    }
}
