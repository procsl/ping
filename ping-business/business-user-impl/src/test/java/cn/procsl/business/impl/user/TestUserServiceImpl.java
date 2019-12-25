package cn.procsl.business.impl.user;

import cn.procsl.business.impl.user.entity.User;
import cn.procsl.business.impl.user.repository.HibernateDaoTemplate;
import cn.procsl.business.impl.user.service.IUserService;
import cn.procsl.business.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author procsl
 * @date 2019/12/17
 */
@Service
public class TestUserServiceImpl implements IUserService {

    @Autowired
    HibernateDaoTemplate<String, User> hibernateDaoTemplate;

    @Autowired
    HibernateTemplate template;

    @Override
    @Transactional
    public void create() {
        User user = new User();
        user.setGender(UserDTO.Gender.男);
        user.setVersion(1L);
        user.setAccount("12312312");
        user.setEmail("program_chen@foxmail.com");
        user.setPhone("hhhhe");
        user.setName("朝闻道");
        user.setPassword("用户密码吗");
        user.setCreateDate(new Date());
        template.save(user);
        hibernateDaoTemplate.save(user);
    }
}
