package cn.procsl.ping.boot.common.event;


import cn.procsl.ping.boot.common.annotation.Publisher;
import cn.procsl.ping.boot.common.aop.AbstractMethodInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class PublisherMethodInterceptor extends AbstractMethodInterceptor<Publisher> {

    final EventBusBridge eventBusBridge;

    final ExpressionParser parser = new SpelExpressionParser();

    final Map<String, Object> rootAttributes = new HashMap<>();
    final StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();

    public PublisherMethodInterceptor(EventBusBridge eventBusBridge,
                                      Collection<PublisherRootAttributeConfigure> configurers) {
        super(Publisher.class);
        this.eventBusBridge = eventBusBridge;
        if (configurers == null || configurers.isEmpty()) {
            return;
        }
        for (PublisherRootAttributeConfigure configurer : configurers) {
            rootAttributes.putAll(configurer.getAttributes());
        }
    }

    @Override
    protected Object doInvoke(Publisher publisher, MethodInvocation invocation) throws Throwable {
        Object returnedValue = null;
        switch (publisher.trigger()) {
            case always:
                try {
                    returnedValue = invocation.proceed();
                } finally {
                    publisher(publisher, invocation, returnedValue);
                }
                break;
            case complete:
                returnedValue = invocation.proceed();
                publisher(publisher, invocation, returnedValue);
                break;
            case error:
                try {
                    returnedValue = invocation.proceed();
                } catch (Exception e) {
                    publisher(publisher, invocation, null);
                    ReflectionUtils.handleReflectionException(e);
                }
                break;
        }
        return returnedValue;
    }

    void publisher(Publisher publisher, MethodInvocation invocation, Object returnedValue) {
        try {

            Serializable parameter = publisher.parameter();
            if (publisher.parameter().isEmpty()) {
                eventBusBridge.publisher(publisher.eventName(), parameter);
                return;
            }

            boolean isSpringEL = publisher.parameter().startsWith("#") || publisher.parameter().startsWith("$");
            if (!isSpringEL) {
                eventBusBridge.publisher(publisher.eventName(), parameter);
                return;
            }

            Object value = evaluation(publisher, invocation, returnedValue);
            if (value != null && !(value instanceof Serializable)) {
                log.warn(String.format("未实现序列化接口:%s ", value.getClass()));
                eventBusBridge.publisher(publisher.eventName(), parameter);
                return;
            }

            eventBusBridge.publisher(publisher.eventName(), (Serializable) value);
        } catch (Exception e) {
            log.error("事件发布出现错误:", e);
        }
    }

    Object evaluation(Publisher publisher, MethodInvocation invocation, Object returnValue) {
        String param = publisher.parameter();
        EvaluationContext context = new MethodBasedEvaluationContext(this.rootAttributes, invocation.getMethod(),
                invocation.getArguments(), discoverer);
        context.setVariable("return", returnValue);
        Expression exp = this.parser.parseExpression(param);
        Object value = exp.getValue(context);
        log.debug("表达式:[{}], 值:[{}]", param, value);
        return value;
    }
}
