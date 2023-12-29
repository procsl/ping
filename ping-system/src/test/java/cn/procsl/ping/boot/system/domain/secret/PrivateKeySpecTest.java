package cn.procsl.ping.boot.system.domain.secret;

import cn.procsl.ping.boot.system.TestSystemApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@Slf4j
@Rollback
@Transactional
@DisplayName("密钥查询测试")
@SpringBootTest(classes = TestSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PrivateKeySpecTest {

    @Inject
    JpaSpecificationExecutor<PrivateKey> jpaSpecificationExecutor;

    @Inject
    JpaRepository<PrivateKey, Long> jpaRepository;

    @Test

    public void toPredicate() {

        long id = System.currentTimeMillis() - 1000;

        jpaRepository.save(PrivateKey.newKey(new Date(id)));
        jpaRepository.save(PrivateKey.newKey(new Date(id)));
        jpaRepository.save(PrivateKey.newKey(new Date(id)));
        jpaRepository.save(PrivateKey.newKey(new Date(id)));
        jpaRepository.save(PrivateKey.newKey(new Date(id)));
        jpaRepository.save(PrivateKey.newKey(new Date(id)));
        jpaRepository.save(PrivateKey.newKey(new Date(id)));
        jpaRepository.save(PrivateKey.newKey(new Date(id)));
        jpaRepository.save(PrivateKey.newKey(new Date(id)));
        jpaRepository.save(PrivateKey.newKey(new Date(id)));
        jpaRepository.save(PrivateKey.newKey(new Date(id)));
        jpaRepository.flush();

        List<PrivateKey> list = jpaRepository.findAll();
        log.info("========================");
        log.info("list1: {}", list);
        log.info("========================");
        list = jpaSpecificationExecutor.findAll(new PrivateKeySpec());
        log.info("list2: {}", list);
        log.info("========================");
        jpaSpecificationExecutor.findAll(new PrivateKeySpec(100L));
        log.info("list3: {}", list);
        log.info("========================");

    }
}