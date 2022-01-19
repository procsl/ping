package cn.procsl.ping.processor.v3.web.parser;

import cn.procsl.ping.processor.v3.ProcessorEnvironment;
import cn.procsl.ping.processor.v3.TypeDescriptor;

import javax.lang.model.element.TypeElement;

public class ServiceElementParser {

    /**
     * 解析所有的服务
     *
     * @param element     目标元素
     * @param environment 编译环境上下文
     * @return 类型描述
     */
    public TypeDescriptor parse(TypeElement element, ProcessorEnvironment environment) {

//
//        if (!environment.isPublic(element)) {
//            return null;
//        }
//
//        if (environment.isStatic(element)) {
//            return null;
//        }
//
//        MethodDescriptorImpl descriptor = new MethodDescriptorImpl(element, environment);
//        if (descriptor.isRequest()) {
//            return descriptor;
//        }

        return null;
    }

}
