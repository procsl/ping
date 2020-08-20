package cn.procsl.ping.boot.domain.support.executor;

import cn.procsl.ping.business.domain.DomainEvents;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;

@Slf4j
public class DomainEventListener {

    @PersistenceContext
    EntityManager entityManager;

    @PostPersist
    public void postPersist(Object events) {
        if (events instanceof DomainEvents) {
            log.info("On new events:{}", events.getClass().getName());
            ((DomainEvents) events).postPersist();
            log.info("Post:{}", events.toString());
        }
    }

    @PrePersist
    public void prePersist(Object events) {
        if (events instanceof DomainEvents) {
            log.info("On new events:{}", events.getClass().getName());
            ((DomainEvents) events).prePersist();
            log.info("pre{}", events.toString());
        }
    }
}
