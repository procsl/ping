package cn.procsl.ping.processor.v3;

import javax.lang.model.element.TypeElement;
import java.util.List;

public interface TypeDescriptor {

    String getPackageName();

    String getClassName();

    TypeElement getTarget();

    List<FieldDescriptor> getFields();

    List<MethodDescriptor> getMethods();

}
