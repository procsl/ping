package cn.procsl.ping.boot.common.validator;

import cn.procsl.ping.boot.common.TestCommonApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;

@Slf4j
@RequiredArgsConstructor
@SpringBootTest(classes = TestCommonApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UniqueValidatorImplTest {

    @Inject
    UniqueValidator uniqueValidator;

    @Inject
    JpaRepository<Unique, Long> jpaRepository;
    Unique unique;

    @BeforeEach
    void setUp() {
        this.unique = jpaRepository.save(new Unique("test"));
    }

    @Test
    public void valid() {
        this.uniqueValidator.valid(Unique.class, unique.getId(), "column", unique.getColumn(), "已存在");
    }
}
