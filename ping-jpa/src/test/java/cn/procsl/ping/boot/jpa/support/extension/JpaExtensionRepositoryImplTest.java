package cn.procsl.ping.boot.jpa.support.extension;

import cn.procsl.ping.boot.jpa.JpaTestApplication;
import cn.procsl.ping.boot.jpa.domain.ExtensionRepository;
import cn.procsl.ping.boot.jpa.domain.TestEntity;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

@Slf4j
@Service
@Validated
@Transactional
@Rollback
@SpringBootTest(classes = JpaTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class JpaExtensionRepositoryImplTest {

    @Inject
    ExtensionRepository extensionRepository;

    @Inject
    JpaRepository<TestEntity, Long> jpaRepository;

    @Inject
    EntityManager entityManager;

    @Test
    public void test2() {
        TestEntity test = entityManager.find(TestEntity.class, 1L);
        log.info("test: {}", test);
    }

    @Test
    public void test() {

        List<TestEntity> all = this.jpaRepository.findAll();
        log.info("all: {}", all);

        all = extensionRepository.findAllBy();
        log.info("all: {}", all);
    }

    @Test
    public void setJpaRepository() {

    }

}