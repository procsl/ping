package cn.procsl.ping.processor.v2.web.request;

import cn.procsl.ping.processor.ProcessorEnvironment;
import cn.procsl.ping.processor.v2.SpecHandler;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Element;
import java.util.function.Supplier;

class PatchMethodHandler implements SpecHandler<Supplier<MethodSpec.Builder>> {


    @Override
    public void handle(Element element, Supplier<MethodSpec.Builder> builder, ProcessorEnvironment environment) {

    }
}
