package cn.procsl.ping.boot.domain.support.exector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;

/**
 * @author procsl
 * @date 2020/08/03
 */
@Slf4j
public class DomainRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends JpaRepositoryFactoryBean<T, S, ID> {

    /**
     * Creates a new {@link JpaRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public DomainRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        DomainRepositoryFactory domainRepositoryFactory = new DomainRepositoryFactory(entityManager);
        domainRepositoryFactory.setEntityPathResolver(this.getEntityPathResolver());
        domainRepositoryFactory.setEscapeCharacter(this.getEscapeCharacter());
        log.info("create domain repository factory.");
        return domainRepositoryFactory;
    }

    protected EntityPathResolver getEntityPathResolver() {
        Field field = ReflectionUtils.findField(JpaRepositoryFactoryBean.class,
                "entityPathResolver", EntityPathResolver.class);
        try {
            field.setAccessible(true);
            EntityPathResolver tmp = (EntityPathResolver) field.get(this);
            field.setAccessible(false);
            return tmp;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("获取entityPathResolver错误", e);
        }
    }

    protected EscapeCharacter getEscapeCharacter() {
        Field field = ReflectionUtils.findField(JpaRepositoryFactoryBean.class,
                "escapeCharacter", EscapeCharacter.class);
        try {
            field.setAccessible(true);
            EscapeCharacter tmp = (EscapeCharacter) field.get(this);
            field.setAccessible(false);
            return tmp;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("获取escapeCharacter错误", e);
        }
    }
}

