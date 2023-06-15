package cn.procsl.ping.boot.jpa.support;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.query.ReturnedType;

public class ReturnTypeWarpper {
    public static ReturnedType of(Class<?> returnedType, Class<?> domainType, ProjectionFactory factory){
//       return  ReturnedType.of(returnedType, domainType,factory);
        return null;
    }
}
