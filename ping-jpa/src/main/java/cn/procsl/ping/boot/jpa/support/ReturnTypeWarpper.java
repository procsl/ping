package cn.procsl.ping.boot.jpa.support;

import lombok.SneakyThrows;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class ReturnTypeWarpper {

    @SneakyThrows
    public static ReturnedType of(Class<?> returnedType, Class<?> domainType, ProjectionFactory factory) {
        Method method = ReturnedType.class.getDeclaredMethod("of", Class.class, Class.class, ProjectionFactory.class);
        return (ReturnedType) ReflectionUtils.invokeMethod(method, null, returnedType, domainType, factory);
//        return ReturnedType.of(returnedType, domainType, factory);
    }
}
