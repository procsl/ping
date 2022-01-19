package cn.procsl.ping.processor.v3;

import javax.lang.model.element.VariableElement;
import java.util.List;

public interface MethodParameterDescriptor {

    List<? extends VariableElement> getParameters();

}
