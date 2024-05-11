package cn.procsl.ping.boot.system.domain.ac.abac;

import cn.procsl.ping.boot.system.domain.ac.Request;
import lombok.NonNull;

import java.util.Collection;

/**
 * 基于属性的访问请求
 */
@FunctionalInterface
public interface AttributeRequest extends Request {

    @NonNull
    Collection<String> getAttributes(@NonNull String attributeName);

    @Override
    default String getSubject() {
        Collection<String> tmp = this.getAttributes("subject");
        if (tmp.isEmpty()) {
            return null;
        }
        return tmp.iterator().next();
    }
}
