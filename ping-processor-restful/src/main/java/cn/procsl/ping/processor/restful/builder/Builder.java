package cn.procsl.ping.processor.restful.builder;

import cn.procsl.ping.processor.Model;
import cn.procsl.ping.processor.model.Type;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

public interface Builder<T extends Model> {

    void build(Type input, Element element, RoundEnvironment roundEnv);

}
