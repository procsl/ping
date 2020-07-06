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
    JpaRepository<Session, Long> identityJpaRepository;

    @Inject
    QuerydslPredicateExecutor<Session> identQueryDsl;

    @Test
    void testAddRole() {
        Session ident = Session.creator().active(true).done();
        Long id = identityJpaRepository.save(ident).getId();

        Optional<Session> ident1 = identityJpaRepository.findById(id);

        ident1.get().addRole(1L);
        log.info("{}", ident1.get());
        identityJpaRepository.flush();
    }

    @Test
    void testRemove() {
        Session ident = Session.creator().active(true).done();
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


        Optional<Session> tmp = identityJpaRepository.findById(id);
        tmp.ifPresent(identity -> {
            identity.remove(1L);
            identity.remove(2L);
            identity.remove(3L);
        });
        Assert.assertNotNull(tmp.get());
        identityJpaRepository.saveAndFlush(ident);

        Optional<Session> tmp2 = identityJpaRepository.findById(id);
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
    void testHasRole() {
        Session ident = Session.creator().active(true).done();
        ident.addRole(1L);
        Long id = identityJpaRepository.save(ident).getId();
        identityJpaRepository.flush();

        Optional<Session> tmp = identityJpaRepository.findById(id);
        Assert.assertNotNull(tmp.get());

        boolean bool = tmp.get().hasRole(1L);
        Assert.assertTrue(bool);

        boolean bool2 = tmp.get().hasRole(2L);
        Assert.assertFalse(bool2);
    }


    @Test
    void enable() {
        Session ident = Session.creator().active(false).done();
        Long id = identityJpaRepository.save(ident).getId();
        identityJpaRepository.flush();

        Session ident2 = identityJpaRepository.findById(id).get();
        Assert.assertFalse(ident2.isActive());

        ident.enable();
        identityJpaRepository.save(ident);

        Session ident3 = identityJpaRepository.findById(id).get();
        Assert.assertTrue(ident3.isActive());
    }

    @Test
    void disable() {
        Session ident = Session.creator().active(false).done();
        Long id = identityJpaRepository.save(ident).getId();
        identityJpaRepository.flush();

        Session ident2 = identityJpaRepository.findById(id).get();
        Assert.assertFalse(ident2.isActive());

        ident.disable();
        identityJpaRepository.save(ident);

        Session ident3 = identityJpaRepository.findById(id).get();
        Assert.assertFalse(ident3.isActive());
    }
}
