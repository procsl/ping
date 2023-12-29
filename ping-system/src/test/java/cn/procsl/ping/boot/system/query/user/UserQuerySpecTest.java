package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.system.TestSystemApplication;
import cn.procsl.ping.boot.system.domain.user.AccountState;
import cn.procsl.ping.boot.system.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Slf4j
@DisplayName("JPA spec 查询测试")
@Transactional
@SpringBootTest(classes = TestSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserQuerySpecTest {


    @Inject
    JpaSpecificationExecutor<User> jpaSpecificationExecutor;

    @Test
    public void toPredicate() {
        UserQuerySpec spec = UserQuerySpec.builder().name("admin").account("name").state(AccountState.enable).build();
        List<User> result = jpaSpecificationExecutor.findAll(spec);
        log.info("result {}", result);
    }
}