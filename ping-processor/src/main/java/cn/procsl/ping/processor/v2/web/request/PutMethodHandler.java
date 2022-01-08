package cn.procsl.ping.processor.v2.web.request;

import cn.procsl.ping.processor.ProcessorEnvironment;
import cn.procsl.ping.processor.v2.SpecHandler;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Element;
import java.util.function.Supplier;

class PutMethodHandler implements SpecHandler<Supplier<MethodSpec.Builder>> {


    /**
     * 生成不同代码片段的处理器
     *
     * @param element
     * @param builder     处理器节点元素
     * @param environment 环境上下文及工具
     */
    @Override
    public void handle(Element element, Supplier<MethodSpec.Builder> builder, ProcessorEnvironment environment) {

    }
}
