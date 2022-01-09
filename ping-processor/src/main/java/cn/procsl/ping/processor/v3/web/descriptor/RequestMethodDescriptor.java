package cn.procsl.ping.processor.v3.web.descriptor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;

public interface RequestMethodDescriptor {

    String getMethodName();

    TypeElement getParentElement();

    ExecutableElement getTargetMethodElement();

    List<String> getThrows();

    String getHttpMethod();

    RequestParameterDescriptor getRequestParameterDescriptor();

    RequestReturnDescriptor getRequestReturnDescriptor();

    String[] findConsumes();

    String[] findProduces();
}
