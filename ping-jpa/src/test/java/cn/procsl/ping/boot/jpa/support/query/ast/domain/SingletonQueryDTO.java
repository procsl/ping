package cn.procsl.ping.boot.jpa.support.query.ast.domain;


import cn.procsl.ping.boot.jpa.support.query.*;
import jakarta.persistence.criteria.JoinType;
import lombok.Data;

import java.io.Serializable;

/**
 * 1.  Teacher as main
 * 2.  Teacher as main
 *     inner join SubEntity as sub on main.id = sub.id
 * 3.  Teacher as main
 *     inner join SubEntity as sub on main.id = sub.id
 *     inner join SubEntity as sub2 on main.id = sub2.id
 *
 */
@Data
@Projection(entity = Teacher.class, alias = "main")
@Join(ref = "main", join = @Projection(entity = SubEntity.class, alias = "sub"))
@Join(ref = "main", type = JoinType.LEFT, join = @Projection(entity = SubEntity.class, alias = "sub2"))
@Join(ref = "sub", join = @Projection(entity = SubEntity.class, alias = "sub1"))
public class SingletonQueryDTO implements Serializable {

    @Order
    @Select
    Long id;

    @Order
    @Select
    @ReferenceBy(ref = "sub", target = "name")
    @Where(groupName = "a")
    @Where(groupName = "b")
    String subName;

    @Order
    @Select
    @ReferenceBy(ref = "sub1", target = "name")
    @Where(condition = ">=", required = true)
    String sub1Name;

    @Order(sort = Order.Sort.desc, order = 1)
    @Select
    @Where(required = true, groupName = "a", condition = "like")
    String desc;

}
