package cn.procsl.ping.boot.admin.advice;

import cn.procsl.ping.boot.admin.domain.rbac.DataPermissionFilter;
import cn.procsl.ping.boot.common.advice.AbstractMethodInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class DatePermissionMethodInterceptor extends AbstractMethodInterceptor<DataPermissionFilter> {

    final ExpressionParser parser = new SpelExpressionParser();

    final Map<String, Object> rootAttributes = new HashMap<>();

    final StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();

    public DatePermissionMethodInterceptor(Collection<DataPermissionRootAttributeRegistry> configurers
    ) {
        super(DataPermissionFilter.class);
        if (configurers == null || configurers.isEmpty()) {
            return;
        }
        for (DataPermissionRootAttributeRegistry configurer : configurers) {
            rootAttributes.putAll(configurer.getAttributes());
        }
    }

    @Override
    protected Object doInvoke(DataPermissionFilter annotation, MethodInvocation invocation) throws Throwable {
        log.info("被调用了:");
        EvaluationContext context = new MethodBasedEvaluationContext(this.rootAttributes, invocation.getMethod(),
                invocation.getArguments(), discoverer);

        context.setVariable(DataPermissionFilter.ARGUMENTS_NAME, invocation.getArguments());
        Expression exp = this.parser.parseExpression(annotation.filter());
        Object value = exp.getValue(context);
        if (Boolean.TRUE.equals(value)) {
            try {
                Expression executor = this.parser.parseExpression(annotation.executor());
                executor.getValue(context);
            } catch (Exception e) {
                log.warn("表达式执行失败:{}", annotation.executor(), e);
            }
        }
        return invocation.proceed();
    }

}
