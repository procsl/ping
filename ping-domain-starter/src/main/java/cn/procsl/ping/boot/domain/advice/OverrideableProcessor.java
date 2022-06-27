package cn.procsl.ping.boot.domain.advice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OverrideableProcessor implements InitializingBean {

    final ApplicationContext applicationContext;

    final Map<Class<?>, Object> cache = new HashMap<>();

    @Around(value = "cn.procsl.ping.boot.domain.advice.OverrideableProcessor.afterPropertiesSet() && @annotation(overrideable)", argNames = "joinPoint,overrideable")
    public Object around(ProceedingJoinPoint joinPoint, Overrideable overrideable) throws Throwable {

        Object forCache = cache.get(overrideable.target());

        if (forCache != null) {
            return ReflectionUtils.invokeMethod(getMethod(joinPoint), forCache, joinPoint.getArgs());
        }

        try {
            Map<String, ?> targets = this.applicationContext.getBeansOfType(overrideable.target());
            if (targets.size() <= 1) {
                return joinPoint.proceed(joinPoint.getArgs());
            }
            Object target = find(joinPoint, targets);
            this.cache.put(overrideable.target(), target);
            return ReflectionUtils.invokeMethod(getMethod(joinPoint), target, joinPoint.getArgs());

        } catch (BeansException e) {
            return joinPoint.proceed(joinPoint.getArgs());
        }
    }

    protected Object find(ProceedingJoinPoint joinPoint, Map<String, ?> targets) {
        HashMap<OverrideTarget.Strategy, Object> map = new HashMap<>();
        for (Object v : targets.values()) {
            if (v == joinPoint.getTarget()) {
                log.debug("跳过源对象");
                continue;
            }

            Method method = getMethod(joinPoint);
            //如果是jdk proxy，上面的method是接口中的方法签名。需要下面这句话，获取实现类中的的方法签名
            val realTarget = ClassUtils.getMostSpecificMethod(method, joinPoint.getTarget().getClass());

            OverrideTarget strategy = AnnotationUtils.findAnnotation(realTarget, OverrideTarget.class);
            if (strategy == null) {
                log.debug("[{}] BusinessAdviceStrategy 不存在", realTarget.getClass().getName());
                continue;
            }
            if (map.containsKey(strategy.strategy())) {
                throw new IllegalStateException(String.format("存在多个 BusinessAdviceStrategy.%s 实例:[%s]", strategy.strategy(), realTarget.getClass().getName()));
            }
            map.put(strategy.strategy(), v);
        }

        Object value = map.get(OverrideTarget.Strategy.primary);
        if (value != null) {
            return value;
        }

        return map.get(OverrideTarget.Strategy.advice);
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod();
    }

    @Override
    @Pointcut("@annotation(cn.procsl.ping.boot.domain.advice.Overrideable)")
    public void afterPropertiesSet() {
    }
}
