package cn.procsl.ping.processor.v3;

import javax.annotation.Nullable;
import javax.lang.model.element.TypeElement;

public interface FieldDescriptor extends TypeNameDescriptor {

    String getFieldName();

    @Nullable
    TypeElement getTarget();

}
