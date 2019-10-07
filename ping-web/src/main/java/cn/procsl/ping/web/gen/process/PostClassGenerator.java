package cn.procsl.ping.web.gen.process;


import cn.procsl.ping.web.gen.ClassGenerator;
import cn.procsl.ping.web.http.annotation.Post;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * @author procsl
 * @date 2019/10/07
 */
public class PostClassGenerator implements ClassGenerator {
    @Override
    public String support() {
        return Post.class.getName();
    }

    @Override
    public void process(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv, TypeElement element) {
        System.out.println();
        System.out.println("process");
    }
}
