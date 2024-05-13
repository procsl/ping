package cn.procsl.ping.boot.common.bridge;


import cn.procsl.ping.boot.common.Publisher;
import cn.procsl.ping.boot.common.aop.AbstractMethodInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ReflectionUtils;

import javax.inject.Provider;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class PublisherMethodInterceptor extends AbstractMethodInterceptor<Publisher> {

    private final ApplicationContext context;

    EventBusBridge publisherProvider;

    final ExpressionParser parser = new SpelExpressionParser();

    final Map<String, Object> rootAttributes = new HashMap<>();

    final StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();

    private volatile boolean init = false;

    public PublisherMethodInterceptor(ApplicationContext applicationContext) {
        super(Publisher.class);
        this.context = applicationContext;
    }

    @Override
    protected Object doInvoke(Publisher publisher, MethodInvocation invocation) throws Throwable {

        if (!init) {
            synchronized (this) {
                if (!init) {
                    this.publisherProvider = this.context.getBean(EventBusBridge.class);
                    Map<String, PublisherRootAttributeConfigure> configurers = this.context.getBeansOfType(PublisherRootAttributeConfigure.class);
                    configurers.forEach((key, value) -> rootAttributes.putAll(value.getAttributes()));
                    this.init = true;
                }
            }
        }

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
                this.publisherProvider.publisher(publisher.eventName(), parameter);
                return;
            }

            boolean isSpringEL = publisher.parameter().startsWith("#") || publisher.parameter().startsWith("$");
            if (!isSpringEL) {
                this.publisherProvider.publisher(publisher.eventName(), parameter);
                return;
            }

            Object value = evaluation(publisher, invocation, returnedValue);
            if (value != null && !(value instanceof Serializable)) {
                log.warn(String.format("未实现序列化接口:%s ", value.getClass()));
                this.publisherProvider.publisher(publisher.eventName(), parameter);
                return;
            }

            this.publisherProvider.publisher(publisher.eventName(), (Serializable) value);
        } catch (Exception e) {
            log.error("事件发布出现错误:", e);
        }
    }

    Object evaluation(Publisher publisher, MethodInvocation invocation, Object returnValue) {
        String param = publisher.parameter();
        EvaluationContext context = new MethodBasedEvaluationContext(this.rootAttributes, invocation.getMethod(), invocation.getArguments(), discoverer);
        context.setVariable("return", returnValue);
        Expression exp = this.parser.parseExpression(param);
        Object value = exp.getValue(context);
        log.debug("表达式:[{}], 值:[{}]", param, value);
        return value;
    }
}
