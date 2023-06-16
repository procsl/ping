package cn.procsl.ping.boot.jpa.support;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.io.Serializable;

@Slf4j
public class JpaExtensionRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements JpaExtensionRepository<T> {
    public JpaExtensionRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

}
