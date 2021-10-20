package cn.procsl.ping.processor.restful.builder;

import cn.procsl.ping.processor.model.Type;

import javax.lang.model.element.TypeElement;

public interface Builder {

    void build(Type input, TypeElement serviceElement);

}
