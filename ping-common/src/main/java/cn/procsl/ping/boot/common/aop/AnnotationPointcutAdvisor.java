package cn.procsl.ping.boot.common.aop;

import lombok.NonNull;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

import java.lang.annotation.Annotation;

public class AnnotationPointcutAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    final AnnotationPointcut pointcut;

    AnnotationPointcutAdvisor(AnnotationPointcut pointcut, Advice advice) {
        setAdvice(advice);
        this.pointcut = pointcut;
    }

    public static AnnotationPointcutAdvisor forAnnotation(Class<? extends Annotation> target, Advice advice) {
        AnnotationPointcut pointcut = new AnnotationPointcut(target);
        return new AnnotationPointcutAdvisor(pointcut, advice);
    }

    @Override
    public @NonNull Pointcut getPointcut() {
        return pointcut;
    }

}
