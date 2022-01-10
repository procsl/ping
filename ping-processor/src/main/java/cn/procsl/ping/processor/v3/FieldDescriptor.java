package cn.procsl.ping.processor.v3;

import javax.lang.model.element.Element;

public interface FieldDescriptor {

    String getName();

    String getFieldName();

    Element getTarget();

}
