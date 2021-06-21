package cn.procsl.ping.processor.generator;

import cn.procsl.ping.processor.GeneratorContext;

public interface SpecBuilder<S, T> {

    void build(GeneratorContext context, S source, T type);

}
