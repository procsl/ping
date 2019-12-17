package cn.procsl.ping.web.gen.process;

import cn.procsl.ping.web.gen.AnnotationProcessor;
import cn.procsl.ping.web.http.annotation.Post;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;

/**
 * @author procsl
 * @date 2019/10/13
 */
public class PostProcessor extends AnnotationProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> tmp = new HashSet<>(1);
        tmp.add(Post.class.getName());
        return tmp;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }
}
