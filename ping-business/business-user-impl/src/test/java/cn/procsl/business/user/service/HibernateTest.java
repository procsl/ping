package cn.procsl.business.user.service;

import cn.procsl.business.user.dto.UserDTO;
import cn.procsl.business.user.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * @author procsl
 * @date 2019/12/16
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@Rollback
public class HibernateTest {

    @Autowired
    SessionFactory sessionFactory;

    @Test
    public void save() {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        User user = new User();
        user.setName("朝闻道");
        user.setCreateDate(new Date());
        user.setPassword("1234567890");
        user.setGender(UserDTO.Gender.未知);
        user.setPhone("15197456789");
        user.setEmail("program_chen@foxmal.com");
        user.setAccount("123123098093");
        user.setVersion(1L);
        session.save(user);
        transaction.commit();
        session.close();
        Assert.assertNotNull(user.getId());
    }

    @Test
    public void update() {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        User user = new User();
        user.setName("朝闻道");
        user.setCreateDate(new Date());
        user.setPassword("1234567890");
        user.setGender(UserDTO.Gender.未知);
        user.setPhone("15197456789");
        user.setEmail("program_chen@foxmal.com");
        user.setAccount("123123098093");
        user.setVersion(1L);
        ;
        transaction.commit();
        session.save(user);
        Assert.assertNotNull(user.getId());

        user.setGender(UserDTO.Gender.男);
        transaction = session.beginTransaction();
        session.update(user);
        transaction.commit();

        user = session.get(User.class, user.getId());
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getGender(), UserDTO.Gender.男);
        session.close();
    }

    @Test
    public void query() {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        User user = new User();
        user.setName("朝闻道");
        user.setCreateDate(new Date());
        user.setPassword("1234567890");
        user.setGender(UserDTO.Gender.未知);
        user.setPhone("15197456789");
        user.setEmail("program_chen@foxmal.com");
        user.setAccount("123123098093");
        user.setVersion(1L);
        transaction.commit();
        session.save(user);
        Assert.assertNotNull(user.getId());

        User getUser = session.get(User.class, user.getId());
        Assert.assertNotNull(getUser);
        Assert.assertEquals(getUser.getGender(), user.getGender());
        Assert.assertEquals(getUser.getAccount(), user.getAccount());

        User loadUser = session.load(User.class, user.getId());
        Assert.assertEquals(user.getGender(), loadUser.getGender());
        Assert.assertEquals(user.getAccount(), loadUser.getAccount());
        session.close();
    }

    @Test
    public void delete() {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        User user = new User();
        user.setName("朝闻道");
        user.setCreateDate(new Date());
        user.setPassword("1234567890");
        user.setGender(UserDTO.Gender.未知);
        user.setPhone("15197456789");
        user.setEmail("program_chen@foxmal.com");
        user.setAccount("123123098093");
        user.setVersion(1L);
        transaction.commit();
        session.save(user);
        Assert.assertNotNull(user.getId());

        session.delete(user);

        User getUser = session.get(User.class, user.getId());
        Assert.assertNull(getUser);
    }

}
