package cn.procsl.ping.boot.user.domain.rbac.entity;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
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
class SessionTest {

    @Inject
    JpaRepository<Session, Long> sessionJpaRepository;

    @Test
    void testAddRole() {
        Session session = Session.creator().active(true).done();
        Long id = sessionJpaRepository.save(session).getId();

        Optional<Session> session1 = sessionJpaRepository.findById(id);

        session1.get().addRole(1L);
        log.info("{}", session1.get());
        sessionJpaRepository.flush();
    }

    @Test
    void testRemove() {
        Session session = Session.creator().active(true).done();
        session.addRole(1L);
        session.addRole(2L);
        session.addRole(3L);
        session.addRole(4L);
        session.addRole(5L);
        session.addRole(6L);
        session.addRole(7L);
        session.addRole(8L);
        Long id = sessionJpaRepository.save(session).getId();
        sessionJpaRepository.flush();


        Optional<Session> tmp = sessionJpaRepository.findById(id);
        tmp.ifPresent(s -> {
            s.remove(1L);
            s.remove(2L);
            s.remove(3L);
        });
        Assert.assertNotNull(tmp.get());
        sessionJpaRepository.saveAndFlush(session);

        Optional<Session> tmp2 = sessionJpaRepository.findById(id);
        tmp2.ifPresent(s -> {
            boolean bool1 = s.getRoles().contains(1L);
            Assert.assertFalse(bool1);


            boolean bool2 = s.getRoles().contains(2L);
            Assert.assertFalse(bool2);


            boolean bool3 = s.getRoles().contains(3L);
            Assert.assertFalse(bool3);


            boolean bool4 = s.getRoles().contains(4L);
            Assert.assertTrue(bool4);
        });
        Assert.assertNotNull(tmp2.get());
    }

    @Test
    void testHasRole() {
        Session session = Session.creator().active(true).done();
        session.addRole(1L);
        Long id = sessionJpaRepository.save(session).getId();
        sessionJpaRepository.flush();

        Optional<Session> tmp = sessionJpaRepository.findById(id);
        Assert.assertNotNull(tmp.get());

        boolean bool = tmp.get().hasRole(1L);
        Assert.assertTrue(bool);

        boolean bool2 = tmp.get().hasRole(2L);
        Assert.assertFalse(bool2);
    }


    @Test
    void enable() {
        Session session = Session.creator().active(false).done();
        Long id = sessionJpaRepository.save(session).getId();
        sessionJpaRepository.flush();

        Session session1 = sessionJpaRepository.findById(id).get();
        Assert.assertFalse(session1.isActive());

        session.enable();
        sessionJpaRepository.save(session);

        Session session2 = sessionJpaRepository.findById(id).get();
        Assert.assertTrue(session2.isActive());
    }

    @Test
    void disable() {
        Session session = Session.creator().active(false).done();
        Long id = sessionJpaRepository.save(session).getId();
        sessionJpaRepository.flush();

        Session session1 = sessionJpaRepository.findById(id).get();
        Assert.assertFalse(session1.isActive());

        session.disable();
        sessionJpaRepository.save(session);

        Session session2 = sessionJpaRepository.findById(id).get();
        Assert.assertFalse(session2.isActive());
    }
}
