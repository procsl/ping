package cn.procsl.ping.boot.rbac.domain.model;


import cn.procsl.ping.boot.rbac.domain.repository.RoleRepository;
import cn.procsl.ping.boot.rbac.domain.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.LinkedList;

/**
 * @author procsl
 * @date 2020/04/08
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class UserTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository repository;

    @Autowired
    @PersistenceContext
    private EntityManager entityManager;

    private JPAQueryFactory queryFactory;

    public void init() {
        queryFactory = new JPAQueryFactory(entityManager);
    }

    @Test
    public void testUser() {
        User user = userRepository.findOne(QUser.user.name.eq("root")).get();
        Assert.assertNotNull(user);

        String[] roleNames = {"role1", "role2", "role3", "role4"};

        // 添加一个测试的权限
        LinkedList<Role> list = new LinkedList<>();
        for (String name : roleNames) {
            Role role = Role.create().name(name).done();
            list.add(role);
        }
        repository.saveAll(list);
        user.addRole(list);
        userRepository.save(user);

        user.removeRole(list.get(2));
        user.removeRole(list.get(3));
        userRepository.save(user);
    }

    @Test
    public void deleteRole() {
        User user = userRepository.findOne(QUser.user.name.eq("root")).get();
        Assert.assertNotNull(user);

        Role role = this.repository.findOne(QRole.role.name.eq("超级管理员")).get();
        Assert.assertNotNull(role);

        user.removeRole(role);
        userRepository.save(user);
    }


}
