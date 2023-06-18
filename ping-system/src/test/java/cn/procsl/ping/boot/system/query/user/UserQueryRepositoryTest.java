package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.system.TestSystemApplication;
import cn.procsl.ping.boot.system.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;


@Slf4j
@DisplayName("JPA投影查询测试")
@Transactional
@SpringBootTest(classes = TestSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserQueryRepositoryTest {


    @Inject
    UserQueryRepository queryRepository;

    @Test
    @Transactional(readOnly = true)
    public void findAll() {
        List<UserRecord> result = queryRepository.findAllBy();
        log.info("test: {}", result);
    }

    @Test
    @Transactional(readOnly = true)
    public void findAll2() {
        Optional<UserRecord> result = queryRepository.findOne((Specification<User>) (root, query, cb) -> cb.equal(root.get("name"), "admin"), UserRecord.class);
        log.info("test: {}", result.get());
    }

    @Test
    public void test() {
        Page<UserRecord> result = queryRepository.findAllByNameAndGenderAndAccountNameAndAccountStateOrderByName("admin", null, null, null, Pageable.ofSize(10));
        log.info("result: {}", result.get());
    }

}