package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.system.TestSystemApplication;
import cn.procsl.ping.boot.system.domain.user.User;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import java.util.List;


@Slf4j
@DisplayName("JPA投影查询测试")
@Transactional
@SpringBootTest(classes = TestSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserQueryRepositoryTest {


    @Inject
    UserQueryRepository queryRepository;

    @Test
    @Transactional(readOnly = true)
    public void findAllByName() {
        List<UserRecord> user = queryRepository.findAll();
        log.info("user 信息:{}", user);
    }
}