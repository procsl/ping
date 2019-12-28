package cn.procsl.business.impl.user;

import cn.procsl.business.impl.user.service.IUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author procsl
 * @date 2019/12/17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/user-business.xml"})
@Rollback
public class HibernateSpringTest {

    @Autowired
    IUserService userService;

    @Test
    public void create() {
        this.userService.create();
    }
}