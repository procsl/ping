package cn.procsl.ping.boot.domain.valid;

import cn.procsl.ping.boot.domain.DomainApplication;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@SpringBootTest(classes = DomainApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TableTest {

    Faker faker = new Faker(Locale.CHINA);
    @Autowired
    JpaRepository<Table, Long> jpaRepository;

    @Test
    @Transactional
    void insert() {
        try {
            String name = faker.name().fullName();
            jpaRepository.save(new Table(10L, name));
            jpaRepository.save(new Table(11L, name));
            jpaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            log.error("唯一索引约束:{}", e.getLocalizedMessage());
            log.error("唯一索引约束异常", e);
        }
    }

}
