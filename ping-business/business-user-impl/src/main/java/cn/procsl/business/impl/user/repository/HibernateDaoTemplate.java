package cn.procsl.business.impl.user.repository;

import cn.procsl.business.impl.user.entity.EntityObject;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

/**
 * @author procsl
 * @date 2019/12/14
 */
@Repository
public class HibernateDaoTemplate<T extends Serializable, Entity extends EntityObject<T>> implements DaoTemplate<T, Entity> {

    @Autowired
    @Setter
    protected HibernateTemplate template;

    @Override
    public void save(Entity entity) {
        this.template.save(entity);
    }

    @Override
    public void update(Entity entity) {
        this.template.update(entity);
    }

    @Override
    public void delete(Entity entity) {
        this.template.delete(entity);
    }

    @Override
    public Entity load(T id) {
        return null;
    }
}
