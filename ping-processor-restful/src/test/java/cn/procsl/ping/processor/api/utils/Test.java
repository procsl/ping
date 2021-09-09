package cn.procsl.ping.processor.api.utils;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeVariableName;

import javax.annotation.processing.Generated;

public class Test {

    @org.junit.Test
    public void tester() {

        TypeVariableName collect = TypeVariableName.get("java.util.Collection");
        collect.annotated(AnnotationSpec.builder(Generated.class).build());
        collect.withoutAnnotations();
//        collect.withBounds()
//        collect.annotations.add(AnnotationSpec.builder(Generated.class).build());
        System.out.printf(collect.toString());

//        JavaFile.builder("name", ).build();
    }

}
