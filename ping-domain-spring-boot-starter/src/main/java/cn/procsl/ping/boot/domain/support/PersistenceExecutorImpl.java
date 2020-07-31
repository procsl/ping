package cn.procsl.ping.boot.domain.support;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/**
 * @author procsl
 * @date 2020/04/12
 */
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PersistenceExecutorImpl<T, ID> implements PersistenceExecutor<T, ID> {

    private final JpaEntityInformation<T, ?> entityInformation;
    private final EntityManager em;
    private final PersistenceProvider provider;

    @Setter
    private CrudMethodMetadata metadata;

    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;
}
