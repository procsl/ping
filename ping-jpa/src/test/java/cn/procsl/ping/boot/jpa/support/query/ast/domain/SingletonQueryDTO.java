package cn.procsl.ping.boot.jpa.support.query.ast.domain;


import cn.procsl.ping.boot.jpa.support.query.*;
import lombok.Data;

import java.io.Serializable;

/**
 * select teacher.id, teacher.name as teacherName, teacher.desc  from Teacher as teacher where id=:id
 */
@Data
@Projection(entity = Teacher.class, alias = "main")
@JoinField(ref = "main", join = @Projection(entity = SubEntity.class, alias = "sub"))
@JoinField(ref = "sub", join = @Projection(entity = SubEntity.class, alias = "sub1"))
public class SingletonQueryDTO implements Serializable {

    @OrderByField
    @SelectField
    Long id;

    @SelectField
    @ReferenceBy(ref = "sub", target = "name")
    @WhereField(groupName = "a")
    @WhereField(groupName = "b")
    String subName;

    @SelectField
    @ReferenceBy(ref = "sub1", target = "name")
    @WhereField(condition = ">=", required = true)
    String sub1Name;

    @SelectField
    @WhereField(required = true, groupName = "a", condition = "like")
    String desc;

}
