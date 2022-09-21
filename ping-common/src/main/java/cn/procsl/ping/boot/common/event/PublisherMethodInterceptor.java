package cn.procsl.ping.boot.common.event;


import cn.procsl.ping.boot.common.advice.AbstractMethodInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class PublisherMethodInterceptor extends AbstractMethodInterceptor<Publisher> {

    final EventBusBridge eventBusBridge;

    final ExpressionParser parser = new SpelExpressionParser();

    public PublisherMethodInterceptor(EventBusBridge eventBusBridge) {
        super(Publisher.class);
        this.eventBusBridge = eventBusBridge;
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
            case complete:
                returnedValue = invocation.proceed();
                publisher(publisher, invocation, returnedValue);
            case error:
                try {
                    returnedValue = invocation.proceed();
                } catch (Exception e) {
                    publisher(publisher, invocation, returnedValue);
                    throw e;
                }
        }
        return returnedValue;
    }

    void publisher(Publisher publisher, MethodInvocation invocation, Object returnedValue) {
        try {

            Serializable parameter = publisher.parameter();
            if (publisher.parameter().isEmpty()) {
                eventBusBridge.publisher(publisher.name(), parameter);
                return;
            }

            boolean isSpel = publisher.parameter().startsWith("#") || publisher.parameter().startsWith("$");
            if (!isSpel) {
                eventBusBridge.publisher(publisher.name(), parameter);
                return;
            }

            Object value = evaluation(publisher, invocation, returnedValue);
            if (value != null && !(value instanceof Serializable)) {
                log.warn(String.format("未实现序列化接口:%s ", value.getClass()));
                eventBusBridge.publisher(publisher.name(), parameter);
                return;
            }

            eventBusBridge.publisher(publisher.name(), (Serializable) value);
        } catch (Exception e) {
            log.error("事件发布出现错误:", e);
        }
    }

    Object evaluation(Publisher publisher, MethodInvocation invocation, Object returnValue) {
        String param = publisher.parameter();
        Object root = getRoot(returnValue);
        StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();
        EvaluationContext context = new MethodBasedEvaluationContext(root, invocation.getMethod(),
                invocation.getArguments(), discoverer);
        Expression exp = this.parser.parseExpression(param);
        Object value = exp.getValue(context);
        log.debug("表达式:[{}], 值:[{}]", param, value);
        return value;
    }

    Object getRoot(Object returnValue) {
        Object root;
        if (returnValue != null) {
            root = Map.of("return", returnValue);
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("return", null);
            root = map;
        }
        return root;
    }

}
