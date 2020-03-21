package cn.procsl.ping.business.user.impl;

import cn.procsl.ping.business.user.impl.entity.User;
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

/**
 * @author procsl
 * @date 2019/12/16
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:user/user.xml"})
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
        transaction.commit();
        session.save(user);
        Assert.assertNotNull(user.getId());

        transaction = session.beginTransaction();
        session.update(user);
        transaction.commit();

        user = session.get(User.class, user.getId());
        Assert.assertNotNull(user);
        session.close();
    }

    @Test
    public void query() {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        User user = new User();
        user.setName("朝闻道");
        transaction.commit();
        session.save(user);
        Assert.assertNotNull(user.getId());

        User getUser = session.get(User.class, user.getId());
        Assert.assertNotNull(getUser);
        session.close();
    }

    @Test
    public void delete() {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        User user = new User();
        user.setName("朝闻道");
        transaction.commit();
        session.save(user);
        Assert.assertNotNull(user.getId());

        session.delete(user);

        User getUser = session.get(User.class, user.getId());
        Assert.assertNull(getUser);
    }

}
