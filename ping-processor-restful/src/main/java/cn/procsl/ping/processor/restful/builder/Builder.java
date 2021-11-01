package cn.procsl.ping.processor.restful.builder;

import cn.procsl.ping.processor.component.TypeComponent;

import javax.lang.model.element.TypeElement;

public interface Builder {

    void build(TypeComponent input, TypeElement serviceElement);

}
