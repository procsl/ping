package cn.procsl.ping.boot.system.domain.ac.abac;

import cn.procsl.ping.boot.system.domain.ac.Effect;
import cn.procsl.ping.boot.system.domain.ac.Resource;
import org.springframework.expression.ExpressionParser;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * 资源访问策略, 为表达式
 */
class DefaultAccessPolicy implements Policy {


    @Nullable
    @Override
    public Effect test(ExpressionParser parser, Map<String, Object> rootContext, AttributeRequest request, Resource resource) {
        return null;
    }
}
