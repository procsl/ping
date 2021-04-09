package cn.procsl.ping.processor;


import javax.annotation.Nonnull;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

public interface Expression<T> {

    T interpret(@Nonnull ProcessingEnvironment environment, @Nonnull RoundEnvironment roundEnvironment, @Nonnull Element root);

}
