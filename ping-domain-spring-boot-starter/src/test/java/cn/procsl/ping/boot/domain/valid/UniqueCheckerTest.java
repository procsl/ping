package cn.procsl.ping.boot.domain.valid;

import cn.procsl.ping.boot.domain.DomainApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.JpaRepository;

@Slf4j
@RequiredArgsConstructor
@SpringBootTest(classes = DomainApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UniqueCheckerTest {

    @Autowired
    UniqueService uniqueService;

    @Autowired
    JpaRepository<Unique, Long> jpaRepository;
    Unique unique;

    @BeforeEach
    void setUp() {
        this.unique = jpaRepository.save(new Unique("test"));
    }

    @Test
    void valid() {
//         new Unique("test");
        Class<? extends Persistable<?>> clazz = Unique.class;
//        this.uniqueService.valid(clazz, unique.getId(), "column", unique, "已存在");
        this.uniqueService.valid(null);
    }
}
