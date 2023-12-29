package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.system.TestSystemApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Slf4j
@DisplayName("JPA接口查询测试")
@Transactional
@SpringBootTest(classes = TestSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserQueryControllerTest {


    @Inject
    UserQueryController userQueryController;

    @Test
    public void findUsers() {
        Page<UserRecord> result = userQueryController.findUsers(Pageable.ofSize(10),
                "admin", "admin", null, null);

        log.info("result: {}", result.get());
    }
}