package cn.procsl.ping.boot.system;

import cn.procsl.ping.boot.user.UserApplication;
import cn.procsl.ping.exception.BusinessException;
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

@Slf4j
@RequiredArgsConstructor
@Transactional
@SpringBootTest(classes = UserApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ConfigServiceTest {


    @Autowired
    ConfigService configService;

    @Autowired
    JpaRepository<Config, Long> jpaRepository;
    private Long gid;


    @BeforeEach
    @Transactional
    void setUp() {
        Config config = Config.builder().key("test-1").type(ConfigType.key_value).text("test=222").description("我是测试配置1").build();
        this.gid = this.configService.create(config);
    }

    @Test
    @Transactional
    @DisplayName("创建配置项")
    @Rollback
    void create() {
        Config config = Config.builder().key("test").type(ConfigType.key_value).text("test=1111").description("我是测试配置").build();
        Long id = this.configService.create(config);

        Config configure = jpaRepository.getById(id);
        Assertions.assertNotNull(configure);
        Assertions.assertEquals(config.getKey(), configure.getKey());
        Assertions.assertEquals(config.getText(), configure.getText());
        Assertions.assertEquals(config.getDescription(), configure.getDescription());
    }

    @Test
    @Transactional
    @DisplayName("创建配置项:重复配置key")
    @Rollback
    void createRepeat() {
        Assertions.assertThrows(BusinessException.class, () -> {
            Config config = Config.builder().key("test").type(ConfigType.key_value).text("test=1111").description("我是测试配置").build();
            this.configService.create(config);
            config = Config.builder().key("test").type(ConfigType.key_value).text("test=1111").description("我是测试配置").build();
            this.configService.create(config);
            this.jpaRepository.flush();
        }, "配置项名称已存在");
    }

    @Test
    void changeConfigure() {
        log.info("hello ");
    }

    @Test
    void deleteConfigure() {
    }

    @Test
    void getConfigByKey() {
    }
}
