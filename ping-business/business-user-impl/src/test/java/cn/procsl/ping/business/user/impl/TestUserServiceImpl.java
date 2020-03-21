package cn.procsl.ping.business.user.impl;

import cn.procsl.ping.business.user.impl.entity.User;
import cn.procsl.ping.business.user.impl.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author procsl
 * @date 2019/12/17
 */
@Service
public class TestUserServiceImpl implements IUserService {

    @Autowired
    HibernateTemplate template;

    @Override
    @Transactional
    public void create() {
        User user = new User();
        user.setName("朝闻道");
        template.save(user);
    }
}
