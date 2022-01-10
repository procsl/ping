package cn.procsl.ping.processor.v3;

import javax.lang.model.element.TypeElement;
import java.util.List;

public interface TypeDescriptor<P extends MethodParameterDescriptor, R extends MethodReturnValueDescriptor> {

    String getPackageName();

    String getClassName();

    TypeElement getTarget();

    List<FieldDescriptor> getFields();

    List<MethodDescriptor<P, R>> getMethods();

}
