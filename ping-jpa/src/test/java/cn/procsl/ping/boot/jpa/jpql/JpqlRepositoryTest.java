package cn.procsl.ping.boot.jpa.jpql;

import cn.procsl.ping.boot.jpa.TestJpaApplication;
import cn.procsl.ping.boot.jpa.domain.TestEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;

@Slf4j
@Service
@Validated
@Transactional
@Rollback
@SpringBootTest(classes = TestJpaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class JpqlRepositoryTest {

    @Inject
    JpqlRepository jpqlRepository;

    @Test
    public void query() {
        TestEntity result = jpqlRepository.query(1L, "name");
        String sql = """ 
                select * from i_test where id=1
                """;
        log.info("result {}", result);
    }
}