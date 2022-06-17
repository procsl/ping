package cn.procsl.ping.boot.base.domain.conf;

import cn.procsl.ping.boot.base.TestBaseApplication;
import com.github.javafaker.Faker;
import com.github.jsonzou.jmockdata.JMockData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Slf4j
@DisplayName("配置项服务测试")
@SpringBootTest(classes = TestBaseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ConfigOptionServiceTest {

    @Autowired
    ConfigOptionService configOptionService;

    @Autowired
    JpaRepository<Config, Long> jpaRepository;

    Faker faker = new Faker(Locale.CHINA);

    private Long gid;

    @BeforeEach
    @Transactional
    public void setUp() {
        String key = JMockData.mock(String.class);
        String content = JMockData.mock(String.class);
        String desc = JMockData.mock(String.class);
        this.gid = this.jpaRepository.save(Config.creator(key, content, desc)).getId();
    }

    @Test
    @Transactional
    @DisplayName("创建配置项")
    @Rollback
    public void testCreate() {
        String key = JMockData.mock(String.class);
        String content = JMockData.mock(String.class);
        String desc = JMockData.mock(String.class);
        Long id = configOptionService.put(key, content, desc);

        Config configure = jpaRepository.getById(id);
        Assertions.assertNotNull(configure);
        Assertions.assertEquals(key, configure.getKey());
        Assertions.assertEquals(content, configure.getContent());
        Assertions.assertEquals(desc, configure.getDescription());

        this.configOptionService.put(JMockData.mock(String.class), null, desc);
        this.configOptionService.put(JMockData.mock(String.class), "", desc);
        this.configOptionService.put(JMockData.mock(String.class), null, null);
        this.configOptionService.put(JMockData.mock(String.class), JMockData.mock(String.class), null);
        this.configOptionService.put(JMockData.mock(String.class), JMockData.mock(String.class), "");
        this.jpaRepository.flush();
    }


    @Test
    @Transactional
    @Rollback
    @DisplayName("修改配置项")
    public void change() {
        String key = JMockData.mock(String.class);
        String content = JMockData.mock(String.class);
        String desc = JMockData.mock(String.class);
        Config config = this.jpaRepository.getById(gid);
        config.edit(key, content, desc);
        List<Config> all = this.jpaRepository.findAll();
        Assertions.assertEquals(1, all.size());
        Assertions.assertEquals(all.get(0).getKey(), key);
        Assertions.assertEquals(all.get(0).getDescription(), desc);
        Assertions.assertEquals(all.get(0).getContent(), content);

        config.edit(key, null, desc);
        config.edit(key, "", desc);
        config.edit(key, content, null);
        config.edit(key, content, "");
        config.edit(JMockData.mock(String.class), content, "");
        config.edit(JMockData.mock(String.class), JMockData.mock(String.class), "");
    }

    @Test
    @Transactional
    @DisplayName("删除配置项")
    @Rollback
    public void delete() {
        this.jpaRepository.deleteById(gid);
        Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> this.jpaRepository.getById(gid));
    }

    @Test
    @Transactional
    @DisplayName("获取配置项")
    @Rollback
    public void getConfig() {

        Config entity = this.jpaRepository.getById(gid);

        String config = this.configOptionService.get(entity.getKey());
        Assertions.assertEquals(config, entity.getContent());

        String config1 = this.configOptionService.get(faker.animal().name());
        Assertions.assertNull(config1);
    }

}