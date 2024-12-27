package cn.procsl.ping.boot.jpa.support.query.ast.domain;


import cn.procsl.ping.boot.jpa.support.query.*;
import lombok.Data;

import java.io.Serializable;

/**
 * select teacher.id, teacher.name as teacherName, teacher.desc  from Teacher as teacher where id=:id
 */
@Data
@Projection(entity = Teacher.class, alias = "teacher")
@JoinField(fromAlias = "main", to = @Projection(entity = SubEntity.class, alias = "sub"))
@JoinField(fromAlias = "main", to = @Projection(entity = SubEntity.class, alias = "sub"))
public class SingletonQueryDTO implements Serializable {

    @OrderByField
    @WhereField
    @SelectField
    Long id;

    @SelectField
    @ReferenceBy(ref = "teacher", target = "name")
    String teacherName;

    @SelectField
    String desc;

}
