package cn.procsl.ping.boot.jpa.support.query;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * ```sql
 * select
 * ProjectionDTO.id,
 * ProjectionDTO.name,
 * ProjectionDTO.desc,
 * subProjection.*
 * from MainEntity as ProjectionDTO inner join SubEntity as subProjection
 * where ProjectionDTO.name like ':name'
 * <order by ProjectionDTO.id aes>
 * <p>
 * ```
 * 组装 selects 之后, 按照顺序生成 select别名, 分别为 a_1, a_2, a_3, a_...
 * <p>
 * 如果字段上没有标注 join, from 则代表当前对象对应的实体字段
 * 当前对象的名称就是 实体对应的别名 例如实体是 MainEntity, 别名是 ProjectionDTO
 * 如果是 from, join标注的字段, 则 字段名是 该实体的别名, 例如: subProjection 是 subEntity的别名
 * 如果是 Collection 类型的属性, 则代表使用单独的查询来组装
 * <p>
 * 基本类型是 String, Number: Int, Long, Short, Byte, Date, Boolean, Char
 * <p>
 * 本查询方法不要求实现所有的查询功能, 只要覆盖大部分场景即可
 */
@Data
@NoArgsConstructor
@From(entity = MainEntity.class, alias = "main")
@Join(ref = "main", join = @From(entity = SubEntity.class, alias = "sub"), rightJoinField = "mainId")
@Join(ref = "sub", join = @From(entity = SubEntity.class, alias = "sub2"), rightJoinField = "id")
@Projection(value = {
    @Projection.Item(value = "sub2.id", alias = "id"),
    @Projection.Item(value = "main.name", alias = "name"),
    @Projection.Item(value = "sub.id", alias = "id2")
}, returnType = RequestParamDTO.class)
public class RequestParamDTO implements Serializable {

    @Order
    Long id;

    @Predicate(operator = Predicate.OperatorType.eq)
    String name;

    @ReferenceBy(ref = "sub")
    String desc;

}
