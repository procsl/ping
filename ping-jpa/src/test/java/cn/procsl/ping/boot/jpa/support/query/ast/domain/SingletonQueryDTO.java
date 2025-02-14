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
@JoinField(ref = "main", join = @Projection(entity = SubEntity.class, alias = "sub"))
@JoinField(ref = "main", joinType = JoinType.LEFT, join = @Projection(entity = SubEntity.class, alias = "sub2"))
@JoinField(ref = "sub", join = @Projection(entity = SubEntity.class, alias = "sub1"))
public class SingletonQueryDTO implements Serializable {

    @OrderByField
    @SelectField
    Long id;

    @OrderByField
    @SelectField
    @ReferenceBy(ref = "sub", target = "name")
    @WhereField(groupName = "a")
    @WhereField(groupName = "b")
    String subName;

    @OrderByField
    @SelectField
    @ReferenceBy(ref = "sub1", target = "name")
    @WhereField(condition = ">=", required = true)
    String sub1Name;

    @OrderByField(sort = OrderByField.Sort.desc, order = 1)
    @SelectField
    @WhereField(required = true, groupName = "a", condition = "like")
    String desc;

}
