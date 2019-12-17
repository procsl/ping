package cn.procsl.business.user.repository;

import cn.procsl.business.user.entity.EntityObject;
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
    public void save(Entity... entities) {
        for (Entity entity : entities) {
            this.template.save(entity);
        }
    }

    @Override
    public void update(Entity... entities) {
        for (Entity entity : entities) {
            this.template.update(entity);
        }
    }

    @Override
    public void delete(Entity... entities) {
        for (Entity entity : entities) {
            this.template.delete(entity);
        }
    }

    @Override
    public Entity load(T id) {
        return null;
    }
}
