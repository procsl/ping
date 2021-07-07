package cn.procsl.ping.processor.api;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;

public interface AnnotationSpecBuilder {

    <T extends Element> void build(ProcessorContext context, @Nullable T source, Object target, String type);

}
