package cn.procsl.ping.boot.system.api.menu;

import cn.procsl.ping.boot.system.TestSystemApplication;
import cn.procsl.ping.boot.system.domain.menu.Menu;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Slf4j
@Transactional
@DisplayName("JPA查询测试")
@SpringBootTest(classes = TestSystemApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MenuTest {


    @Inject
    JpaRepository<Menu, Long> jpaRepository;

    @Test
    public void createPathNodeByCurrent() {
        Menu root = jpaRepository.save(Menu.createRoot("root", "/root"));

        Menu first = jpaRepository.save(root.createChild("11111", "/111"));

        Menu second = jpaRepository.save(first.createChild("2222222", "/2222"));

        log.info("root -> {}, list -> {}", root, root.getPath());
        log.info("first -> {}, list -> {}", first, first.getPath());
        log.info("second -> {}, list -> {}", second, second.getPath());
    }

}
