package cn.procsl.ping.processor.api;

import javax.lang.model.element.Element;

public interface VisitorCallback {

    void callVisitor(Element element, Object builder);

}
