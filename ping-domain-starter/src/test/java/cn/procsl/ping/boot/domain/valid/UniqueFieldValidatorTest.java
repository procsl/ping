package cn.procsl.ping.boot.domain.valid;

import cn.procsl.ping.boot.domain.DomainApplication;
import cn.procsl.ping.boot.domain.utils.ContextHolder;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@SpringBootTest(classes = DomainApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UniqueFieldValidatorTest {

    @Autowired
    JpaRepository<Unique, Long> jpaRepository;

    @Autowired
    EntityManagerFactory factory;

    @Autowired
    EntityManager springEntityManager;

    @Autowired
    Validator validator;

    @Autowired
    UniqueService service;

    Long gid;


    Faker faker = new Faker(Locale.CHINA);

    String key = faker.name().fullName();

    @BeforeEach
    void setUp() {

        EntityManager entityManager = factory.createEntityManager();

        EntityTransaction transaction = entityManager.getTransaction();
        entityManager.clear();
        transaction.begin();
        Assertions.assertNotNull(jpaRepository);

        Unique unique = new Unique(key, Collections.singleton("helloWorld"));
        entityManager.persist(unique);
        gid = unique.getId();

        transaction.commit();
        entityManager.close();
    }

    @Test
    @DisplayName("UniqueField单元测试:entity")
    @Rollback
    @Transactional
    public void isValid() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            try {
                log.debug("Test is running");
                jpaRepository.save(new Unique(key, Collections.emptySet()));
                jpaRepository.flush();

                EntityManager entityManager = ContextHolder.getEntityManager();
                Assertions.assertNotNull(entityManager);
                Unique unique = entityManager.find(Unique.class, gid);
                Unique jpaUnique = jpaRepository.getById(gid);
                Assertions.assertEquals(jpaUnique, unique);
            } catch (Exception e) {
                log.error("校验输出:", e);
                throw e;
            }
        });

    }

    @Test
    @DisplayName("UniqueField单元测试:DTO")
    @Rollback
    @Transactional
    public void isValidDTO() {
        {
            UniqueDTO dto = new UniqueDTO(key);
            Set<ConstraintViolation<UniqueDTO>> errors = this.validator.validate(dto);
            Assertions.assertNotNull(errors);
            Assertions.assertEquals(1, errors.size());
            log.error("错误消息:{}", errors);
        }

        {
            UniqueDTO dto = new UniqueDTO(gid, key);
            Set<ConstraintViolation<UniqueDTO>> errors = this.validator.validate(dto);
            Assertions.assertNotNull(errors);
            Assertions.assertEquals(0, errors.size());
            log.error("错误消息:{}", errors);
        }

        {
            UniqueDTO dto1 = new UniqueDTO("我是唯二");
            Set<ConstraintViolation<UniqueDTO>> errors1 = this.validator.validate(dto1);
            Assertions.assertNotNull(errors1);
            Assertions.assertEquals(0, errors1.size());
            log.error("错误消息1:{}", errors1);
        }
    }

    @Test
    @DisplayName("UniqueField单元测试:method")
    @Rollback
    @Transactional
    public void isValidMethod() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            try {
                this.service.create(key);
            } catch (Exception e) {
                log.error("错误消息:", e);
                throw e;
            }
        });

        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            try {
                this.service.create(new Unique(key));
            } catch (Exception e) {
                log.error("错误消息:", e);
                throw e;
            }
        });

        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            try {
                this.service.create(new UniqueDTO(key));
            } catch (Exception e) {
                log.error("错误消息:", e);
                throw e;
            }
        });

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            try {
                this.service.create(new Unique(gid, key));
            } catch (Exception e) {
                log.error("错误消息:", e);
                throw e;
            }
        }, "测试异常");

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            try {
                this.service.create(new UniqueDTO(gid, key));
            } catch (Exception e) {
                log.error("错误消息:", e);
                throw e;
            }
        }, "测试异常");
    }
}
