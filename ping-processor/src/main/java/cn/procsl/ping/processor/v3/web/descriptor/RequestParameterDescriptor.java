package cn.procsl.ping.processor.v3.web.descriptor;

import javax.lang.model.element.VariableElement;
import java.util.List;

public interface RequestParameterDescriptor {

    RequestMethodDescriptor getMethodDescriptor();

    List<? extends VariableElement> getParameters();

}
