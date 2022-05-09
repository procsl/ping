package cn.procsl.ping.boot.domain.valid;

import cn.procsl.ping.boot.domain.utils.ContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Persistable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class UniqueFieldValidator implements ConstraintValidator<UniqueField, Object> {

    UniqueField unique;
    Entity entity;
    UniqueChecker checker;

    static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
    }

    @Override
    public synchronized void initialize(UniqueField uniqueField) {
        log.debug("初始化数据:[{}]", uniqueField.fieldName());

        this.entity = AnnotationUtils.findAnnotation(uniqueField.entity(), Entity.class);
        if (entity == null) {
            throw new IllegalArgumentException(uniqueField.entity().getName() + "is not a entity");
        }

        this.unique = uniqueField;
        EntityManager entityManager;
        if (uniqueField.useSpringEntityManager()) {
            entityManager = ContextHolder.getApplicationContext().getBean(EntityManager.class);
        } else {
            entityManager = ContextHolder.getEntityManager();
        }

        this.checker = new UniqueChecker(entityManager);
    }

    /**
     * 校验方式:
     * 如果是普通校验 string long int enum, 则直接使用 当前值作唯一校验
     * 如果是对象校验 实现了 id 接口的对象, 则使用 id + 值作为校验
     *
     * @param target  object to validate
     * @param context context in which the constraint is evaluated
     * @return
     */
    @Override
    public boolean isValid(Object target, ConstraintValidatorContext context) {

        log.debug("开始校验数据:{}", target);
        if (target == null) {
            return true;
        }

        AtomicBoolean result = new AtomicBoolean(true);
        if (target instanceof Persistable) {
            // 获取目标对象值
            Object data = getObjectData(target, unique.fieldName());
            Object tid = getObjectData(target, "id");
            Optional<?> option = checker.query(unique.entity(), unique.fieldName(), data);
            result.set(option.isEmpty());
            option.ifPresent(id -> result.set(ObjectUtils.nullSafeEquals(id, tid)));
        } else {
            Optional<?> option = checker.query(unique.entity(), unique.fieldName(), target);
            result.set(option.isEmpty());
        }

        if (!result.get()) {
            message(context, unique.fieldName());
        }
        return result.get();
    }

    private Object getObjectData(Object target, String field) {
        String name = String.format("get%s", toUpperCaseFirstOne(field));
        Method getter = ReflectionUtils.findMethod(target.getClass(), name);
        if (getter == null) {
            throw new IllegalArgumentException("Not fount getter[" + name + "]");
        }
        return ReflectionUtils.invokeMethod(getter, target);
    }

    void message(ConstraintValidatorContext context, String property) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(unique.message()).addPropertyNode(property).addConstraintViolation();
    }

}
