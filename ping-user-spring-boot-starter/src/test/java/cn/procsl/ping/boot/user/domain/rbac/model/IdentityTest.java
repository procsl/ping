package cn.procsl.ping.boot.user.domain.rbac.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author procsl
 * @date 2020/04/28
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Slf4j
class IdentityTest {

    @Inject
    JpaRepository<Identity, Long> identityJpaRepository;

    @Inject
    QuerydslPredicateExecutor<Identity> identQueryDsl;
    private Long id;


    @Before
    public void before() {
        Identity ident = new Identity();
        id = identityJpaRepository.save(ident).getId();
    }

    @Test
    void addRole() {
        Identity ident = new Identity();
        id = identityJpaRepository.save(ident).getId();

        Optional<Identity> ident1 = identityJpaRepository.findById(id);

        ident1.get().addRole(1L);
        log.info("{}",ident1.get());
        identityJpaRepository.flush();
    }

    @Test
    void remove() {
    }

    @Test
    void hasRole() {
    }
}