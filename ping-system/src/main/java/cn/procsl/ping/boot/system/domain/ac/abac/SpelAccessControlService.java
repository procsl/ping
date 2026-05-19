package cn.procsl.ping.boot.system.domain.ac.abac;

import cn.procsl.ping.boot.system.domain.ac.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
class SpelAccessControlService implements AccessControlService {


    final ExpressionParser parser = new SpelExpressionParser();

    final Map<String, Object> rootContext = new HashMap<>();

    final Policy accessPolicy;


    @Override
    @NonNull
    public Effect matcher(@NonNull Request request, @NonNull Resource resource) throws AccessControlException {
        if (!(request instanceof AttributeRequest)) {
            throw new AccessControlException("不支持的Request请求");
        }

        try {
            Effect effect = accessPolicy.test(parser, rootContext, (AttributeRequest) request, resource);
            if (effect == null) {
                return Effect.denied;
            }

            return effect;
        } catch (Exception e) {
            if (e instanceof AccessControlException) {
                throw (AccessControlException) e;
            }
            throw new AccessControlException("策略规则执行失败", e);
        }
    }

}
