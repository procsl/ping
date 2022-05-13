package cn.procsl.ping.boot.domain.valid;

import org.springframework.data.domain.Persistable;

import javax.validation.ConstraintViolationException;
import java.io.Serializable;

public interface UniqueValidation {
    void valid(Class<? extends Persistable<? extends Serializable>> entity, Serializable id, String field, Object filedValue, String message) throws ConstraintViolationException;

}
