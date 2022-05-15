package cn.procsl.ping.boot.domain.jpaspec;

import cn.procsl.ping.boot.domain.DomainApplication;
import com.github.jsonzou.jmockdata.JMockData;
import com.github.jsonzou.jmockdata.MockConfig;
import com.github.wenhao.jpa.Specifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@SpringBootTest(classes = DomainApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Rollback
@Transactional
@Configuration
public class JpaSpecTest {

    @Autowired
    JpaSpecificationExecutor<JpaSpec> jpaSpecificationExecutor;

    @Autowired
    JpaRepository<JpaSpec, Long> jpaRepository;

    @BeforeEach
    void setUp() {
        log.info("开始测试:setUp");
        MockConfig config = new MockConfig().globalConfig().excludes("id");
        ArrayList<JpaSpec> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            JpaSpec data = JMockData.mock(JpaSpec.class, config);
            list.add(data);
            log.info("实体:{}", data);
        }
        jpaRepository.saveAll(list);
        List<JpaSpec> all = jpaRepository.findAll();
        Assertions.assertNotEquals(0, all.size());
        log.info("结束:setUp");
    }

    @Test
    public void test() {
        Specification<JpaSpec> specs = Specifications.<JpaSpec>and().like(true, "name", "%1%").build();
        List<JpaSpec> result = jpaSpecificationExecutor.findAll(specs);
        for (JpaSpec spec : result) {
            log.info("结果:{}", spec);
        }
    }
}
