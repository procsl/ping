package cn.procsl.ping.processor.api;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;

public abstract class AbstractAnnotationSpecBuilder<T> implements AnnotationSpecBuilder {

    @Override
    public final <E extends Element> void build(ProcessorContext context, @Nullable E source, Object target, String type) {
        if (!this.isType(type)) {
            return;
        }

        if (!required()) {
            return;
        }

        if (this.target().equals(target.getClass())) {
            buildTargetAnnotation(context, source, (T) target);
        }
    }

    public boolean required() {
        return true;
    }

    abstract protected boolean isType(String type);

    abstract protected <E extends Element> void buildTargetAnnotation(ProcessorContext context, E source, T target);

    abstract protected Class<T> target();

}
