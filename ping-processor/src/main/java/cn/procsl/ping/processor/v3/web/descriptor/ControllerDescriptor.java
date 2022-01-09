package cn.procsl.ping.processor.v3.web.descriptor;

import java.util.List;

public interface ControllerDescriptor {

    String getControllerName();

    List<RequestMethodDescriptor> getMethods();


}
