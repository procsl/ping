package cn.procsl.ping.processor.restful.builder;

import cn.procsl.ping.processor.model.Annotation;
import cn.procsl.ping.processor.model.Field;
import cn.procsl.ping.processor.model.TypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Collection;

public class ServiceTypeField extends Field {

    final TypeElement typeElement;

    public ServiceTypeField(TypeElement serviceElement) {
        this.typeElement = serviceElement;
    }

    @Override
    public Collection<Annotation> getFieldAnnotations() {
        return null;
    }

    @Override
    public TypeName getType() {
        return super.getType();
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public Modifier getModifier() {
        return super.getModifier();
    }
}
