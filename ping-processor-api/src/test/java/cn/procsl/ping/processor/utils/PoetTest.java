package cn.procsl.ping.processor.utils;

import com.squareup.javapoet.AnnotationSpec;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.annotation.processing.Generated;

@Slf4j
public class PoetTest {


    @Test
    public void test() {
        AnnotationSpec annotation = AnnotationSpec.builder(Generated.class).addMember("", "value={\"哈哈哈哈哈\"}").build();
        String str = annotation.toString();
        System.out.println(str);
    }

}
