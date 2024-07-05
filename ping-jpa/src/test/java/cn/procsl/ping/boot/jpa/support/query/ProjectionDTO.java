package cn.procsl.ping.boot.jpa.support.query;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Projection(entity = MainEntity.class)
public class ProjectionDTO implements Serializable {

    @OrderBy(order = OrderBy.Sort.desc)
    Long id;

    @Where(predicate = Where.WherePredicate.like)
    String name;

    String desc;

//    @JoinOn
//    SubProjection subProjection;
//
//    @JoinOn
//    Collection<SubProjection> subProjections;


    @SelectFields
    public ProjectionDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Data
    @Projection(entity = SubEntity.class)
    public static class SubProjection implements Serializable {

        Long id;

        @Column(length = 100)
        String name;


        String desc;
    }

}
