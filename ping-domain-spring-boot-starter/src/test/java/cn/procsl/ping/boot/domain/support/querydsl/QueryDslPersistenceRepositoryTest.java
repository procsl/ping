package cn.procsl.ping.boot.domain.support.querydsl;

import cn.procsl.ping.boot.domain.domain.entity.QUser;
import cn.procsl.ping.boot.domain.domain.entity.User;
import cn.procsl.ping.boot.domain.support.exector.DomainRepositoryFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
@SpringBootApplication
@EnableJpaRepositories(
        basePackages = "cn.procsl.ping.boot.domain.domain.repository",
        repositoryFactoryBeanClass = DomainRepositoryFactoryBean.class,
        bootstrapMode = BootstrapMode.LAZY
)
@EntityScan(basePackages = "cn.procsl.ping.boot.domain.domain.entity")
public class QueryDslPersistenceRepositoryTest {

    @Inject
    QueryDslPersistenceRepository<User, String> queryDslPersistenceRepository;

    @Test
    public void update() {
        User entity = new User();
        entity.setName("user");
        queryDslPersistenceRepository.update(entity, QUser.user.name.eq("user"));
    }

    @Test
    public void testUpdate() {
        User entity = new User();
        entity.setName("user");
        entity.setCreateDate(new Date());
        queryDslPersistenceRepository.update(entity, QUser.user.name.eq("user"), QUser.user.createDate);
    }

    @Test
    public void testUpdate1() {
        User entity = new User();
        entity.setName("user");
        entity.setCreateDate(new Date());
        queryDslPersistenceRepository.update(entity,
                QUser.user.name.eq("user"), "createDate");
    }

    @Test
    public void updateNotNull() {
        User entity = new User();
        entity.setName("user");
        entity.setCreateDate(new Date());
        queryDslPersistenceRepository.updateNotNull(entity, QUser.user.name.eq("user"));
    }

    @Test
    public void updateExclude() {
        User entity = new User();
        entity.setName("user");
        entity.setCreateDate(new Date());
        queryDslPersistenceRepository.updateExclude(entity,
                QUser.user.name.eq("user"), "user");
    }

    @Test
    public void testUpdateExclude() {
        User entity = new User();
        entity.setName("user");
        entity.setCreateDate(new Date());
        queryDslPersistenceRepository.updateExclude(entity,
                QUser.user.name.eq("user"), QUser.user.name);
    }

    @Test
    public void delete() {
        User entity = new User();
        entity.setName("user");
        entity.setCreateDate(new Date());
        queryDslPersistenceRepository.delete(QUser.user.name.eq("user"));
    }
}
