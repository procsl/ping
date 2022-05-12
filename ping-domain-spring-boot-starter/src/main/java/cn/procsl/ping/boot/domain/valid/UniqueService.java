package cn.procsl.ping.boot.domain.valid;

import javax.validation.ConstraintViolationException;

public interface UniqueService {

    void valid(Class<?> entity) throws ConstraintViolationException;

//    void valid(Persistable<? extends Serializable> entity, String uniqueFieldName) throws ConstraintViolationException;

}
