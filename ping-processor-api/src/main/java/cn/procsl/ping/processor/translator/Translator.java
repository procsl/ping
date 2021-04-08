package cn.procsl.ping.processor.translator;

import javax.annotation.Nonnull;

public interface Translator<T> {

    @Nonnull
    T translate();

}
