package cn.procsl.ping.processor;

import java.io.IOException;

public interface CodeGenerator {

    void generate(GeneratorContext generatorContext) throws IOException;

    String getSupportAnnotation();
}
