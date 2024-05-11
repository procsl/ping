package cn.procsl.ping.boot.system.domain.ac.abac;

import cn.procsl.ping.boot.system.domain.ac.Effect;
import cn.procsl.ping.boot.system.domain.ac.Resource;
import org.springframework.expression.ExpressionParser;

import javax.annotation.Nullable;
import java.util.Map;

interface Policy {

    @Nullable
    Effect test(ExpressionParser parser, Map<String, Object> rootContext, AttributeRequest request, Resource resource);

}
