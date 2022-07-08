package cn.procsl.ping.boot.common.validator;

import org.springframework.data.domain.Persistable;

import javax.validation.ConstraintViolationException;
import java.io.Serializable;

public interface UniqueValidator {
    void valid(Class<? extends Persistable<? extends Serializable>> entity, Serializable id, String field, Object filedValue, String message) throws ConstraintViolationException;

}
