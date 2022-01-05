package cn.procsl.ping.processor;

import com.squareup.javapoet.TypeSpec;
import lombok.NonNull;

import javax.lang.model.element.TypeElement;


/**
 * 生成器, 用于生成源码
 */
public interface TypeSpecBuilder {

    TypeSpec build(@NonNull TypeElement element, @NonNull ProcessorEnvironment env);

}
