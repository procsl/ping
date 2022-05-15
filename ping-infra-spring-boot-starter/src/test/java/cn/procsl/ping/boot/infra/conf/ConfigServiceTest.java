package cn.procsl.ping.boot.infra.conf;

import cn.procsl.ping.boot.infra.InfraApplication;
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

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Locale;

@Slf4j
@DisplayName("配置项服务测试")
@SpringBootTest(classes = InfraApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ConfigServiceTest {

    @Autowired
    ConfigService configService;

    @Autowired
    JpaRepository<Config, Long> jpaRepository;

    Faker faker = new Faker(Locale.CHINA);

    Long gid;

    @BeforeEach
    @Transactional
    public void setUp() {
        String key = JMockData.mock(String.class);
        String content = JMockData.mock(String.class);
        String desc = JMockData.mock(String.class);
        gid = this.configService.add(key, content, desc);
    }

    @Test
    @Transactional
    @DisplayName("创建配置项")
    @Rollback
    public void testCreate() {
        String key = JMockData.mock(String.class);
        String content = JMockData.mock(String.class);
        String desc = JMockData.mock(String.class);
        Long id = configService.add(key, content, desc);

        Config configure = jpaRepository.getById(id);
        Assertions.assertNotNull(configure);
        Assertions.assertEquals(key, configure.getKey());
        Assertions.assertEquals(content, configure.getContent());
        Assertions.assertEquals(desc, configure.getDescription());

        this.configService.add(JMockData.mock(String.class), null, desc);
        this.configService.add(JMockData.mock(String.class), "", desc);
        this.configService.add(JMockData.mock(String.class), null, null);
        this.configService.add(JMockData.mock(String.class), JMockData.mock(String.class), null);
        this.configService.add(JMockData.mock(String.class), JMockData.mock(String.class), "");
        this.jpaRepository.flush();
    }

    @Test
    @Transactional
    @DisplayName("创建配置项:重复配置key")
    @Rollback
    public void createRepeat() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            String key = JMockData.mock(String.class);
            String content = JMockData.mock(String.class);
            String desc = JMockData.mock(String.class);
            this.configService.add(key, content, desc);
            this.configService.add(key, content, desc);
            this.configService.add(key, content, desc);
            this.jpaRepository.flush();
        });
    }


    @Test
    @DisplayName("修改配置项")
    public void change() {
        String key = JMockData.mock(String.class);
        String content = JMockData.mock(String.class);
        String desc = JMockData.mock(String.class);
        configService.edit(gid, key, content, desc);
        List<Config> all = this.jpaRepository.findAll();
        Assertions.assertEquals(1, all.size());
        Assertions.assertEquals(all.get(0).getKey(), key);
        Assertions.assertEquals(all.get(0).getDescription(), desc);
        Assertions.assertEquals(all.get(0).getContent(), content);

        configService.edit(gid, key, null, desc);
        configService.edit(gid, key, "", desc);
        configService.edit(gid, key, content, null);
        configService.edit(gid, key, content, "");
        configService.edit(gid, JMockData.mock(String.class), content, "");
        configService.edit(gid, JMockData.mock(String.class), JMockData.mock(String.class), "");
        Assertions.assertThrows(EntityNotFoundException.class, () -> configService.edit(JMockData.mock(Long.class), JMockData.mock(String.class), JMockData.mock(String.class), ""));
    }

    @Test
    @Transactional
    @DisplayName("删除配置项")
    @Rollback
    public void delete() {
        this.configService.delete(gid);
        Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> this.jpaRepository.getById(gid));
    }

    @Test
    @Transactional
    @DisplayName("获取配置项")
    @Rollback
    public void getConfig() {

        Config entity = this.jpaRepository.getById(gid);

        String config = this.configService.getConfig(entity.getKey());
        Assertions.assertEquals(config, entity.getContent());

        String config1 = this.configService.getConfig(faker.animal().name());
        Assertions.assertNull(config1);
    }

}
