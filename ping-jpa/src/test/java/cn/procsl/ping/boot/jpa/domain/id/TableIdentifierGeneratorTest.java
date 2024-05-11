package cn.procsl.ping.boot.jpa.domain.id;

import cn.procsl.ping.boot.jpa.TestJpaApplication;
import cn.procsl.ping.boot.jpa.support.IdentifierGenerator;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Validated
@Transactional
@Rollback
@SpringBootTest(classes = TestJpaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TableIdentifierGeneratorTest {

    @Inject
    IdentifierGenerator<Long> idGenerator;

    @Inject
    EntityManager entityManager;

    @RepeatedTest(10)
    public void nextId() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Long a = idGenerator.nextId("test", 10L);
            log.info("nextId: {}", i);
            ids.add(a);
        }
        log.info("nextId: {}", ids);
    }

}
