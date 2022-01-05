package cn.procsl.ping.processor;

import lombok.NonNull;

import javax.lang.model.element.TypeElement;
import java.io.IOException;


/**
 * 生成器, 用于生成源码
 */
public interface TypeSpecBuilder {

    void create(@NonNull TypeElement element, @NonNull ProcessorEnvironment env) throws IOException;
}
