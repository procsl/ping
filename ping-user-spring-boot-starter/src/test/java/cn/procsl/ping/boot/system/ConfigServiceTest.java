package cn.procsl.ping.boot.system;

import cn.procsl.ping.boot.user.UserApplication;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
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

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Locale;

@Slf4j
@Transactional
@RequiredArgsConstructor
@SpringBootTest(classes = UserApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ConfigServiceTest {


    @Autowired
    ConfigService configService;

    @Autowired
    JpaRepository<Config, Long> jpaRepository;

    Faker faker = new Faker(Locale.CHINA);

    Long gid;
    ConfigDTO gdto;


    @BeforeEach
    @Transactional
    void setUp() {
        this.gdto = new ConfigDTO(faker.name().fullName(), faker.address().fullAddress(), faker.address().secondaryAddress());
        this.gid = this.configService.add(gdto);
    }

    @Test
    public
    @Transactional
    @DisplayName("创建配置项")
    @Rollback
    void testCreate() {
        ConfigDTO dto = new ConfigDTO(faker.name().fullName(), faker.address().fullAddress(), faker.address().secondaryAddress());
        Long id = this.configService.add(dto);

        Config configure = jpaRepository.getById(id);
        Assertions.assertNotNull(configure);
        Assertions.assertEquals(dto.getKey(), configure.getKey());
        Assertions.assertEquals(dto.getContent(), configure.getContent());
        Assertions.assertEquals(dto.getDescription(), configure.getDescription());
    }

    @Test
    public
    @Transactional
    @DisplayName("创建配置项:重复配置key")
    @Rollback
    void createRepeat() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            ConfigDTO dto = new ConfigDTO(faker.name().fullName(), faker.address().fullAddress(), faker.address().secondaryAddress());
            this.configService.add(dto);
            this.configService.add(dto);
            this.jpaRepository.flush();
        });
    }


    @Test
    public
    @DisplayName("修改配置项")
    void change() {
        ConfigDTO dto = new ConfigDTO(faker.name().fullName(), faker.address().fullAddress(), faker.address().secondaryAddress());
        configService.edit(gid, dto);
        List<Config> all = this.jpaRepository.findAll();
        Assertions.assertEquals(1, all.size());
        Assertions.assertEquals(all.get(0).getKey(), dto.getKey());
        Assertions.assertEquals(all.get(0).getDescription(), dto.getDescription());
        Assertions.assertEquals(all.get(0).getContent(), dto.getContent());
    }

    @Test
    public
    @Transactional
    @DisplayName("删除配置项")
    @Rollback
    void delete() {
        this.configService.delete(gid);
        Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> this.jpaRepository.getById(gid));
    }

    @Test
    public
    @Transactional
    @DisplayName("获取配置项")
    @Rollback
    void getConfig() {
        String config = this.configService.getConfig(gdto.getKey());
        Assertions.assertEquals(config, gdto.getContent());

        String config1 = this.configService.getConfig(faker.animal().name());
        Assertions.assertNull(config1);
    }
}
