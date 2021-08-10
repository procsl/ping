package cn.procsl.ping.processor.api;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

public interface ComposeGenerator<T> {

    T generated(TypeElement target, RoundEnvironment roundEnv);

}
