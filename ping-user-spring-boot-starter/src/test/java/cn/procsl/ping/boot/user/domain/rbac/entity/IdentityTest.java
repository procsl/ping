package cn.procsl.ping.boot.user.domain.rbac.entity;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Optional;

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

    @Test
    void addRole() {
        Identity ident = new Identity();
        Long id = identityJpaRepository.save(ident).getId();

        Optional<Identity> ident1 = identityJpaRepository.findById(id);

        ident1.get().addRole(1L);
        log.info("{}", ident1.get());
        identityJpaRepository.flush();
    }

    @Test
    void remove() {
        Identity ident = new Identity();
        ident.addRole(1L);
        ident.addRole(2L);
        ident.addRole(3L);
        ident.addRole(4L);
        ident.addRole(5L);
        ident.addRole(6L);
        ident.addRole(7L);
        ident.addRole(8L);
        Long id = identityJpaRepository.save(ident).getId();
        identityJpaRepository.flush();


        Optional<Identity> tmp = identityJpaRepository.findById(id);
        tmp.ifPresent(identity -> {
            identity.remove(1L);
            identity.remove(2L);
            identity.remove(3L);
        });
        Assert.assertNotNull(tmp.get());
        identityJpaRepository.saveAndFlush(ident);

        Optional<Identity> tmp2 = identityJpaRepository.findById(id);
        tmp2.ifPresent(identity -> {
            boolean bool1 = identity.getRoles().contains(1L);
            Assert.assertFalse(bool1);


            boolean bool2 = identity.getRoles().contains(2L);
            Assert.assertFalse(bool2);


            boolean bool3 = identity.getRoles().contains(3L);
            Assert.assertFalse(bool3);


            boolean bool4 = identity.getRoles().contains(4L);
            Assert.assertTrue(bool4);
        });
        Assert.assertNotNull(tmp2.get());
    }

    @Test
    void hasRole() {
        Identity ident = new Identity();
        ident.addRole(1L);
        Long id = identityJpaRepository.save(ident).getId();
        identityJpaRepository.flush();

        Optional<Identity> tmp = identityJpaRepository.findById(id);
        Assert.assertNotNull(tmp.get());

        boolean bool = tmp.get().hasRole(1L);
        Assert.assertTrue(bool);

        boolean bool2 = tmp.get().hasRole(2L);
        Assert.assertFalse(bool2);
    }
}
