package cn.procsl.ping.boot.jpa.support.query;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.Collection;

@Data
@NoArgsConstructor
@Projection(entity = MainEntity.class)
public class ProjectionDTO implements Serializable {

    @OrderBy
    Long id;

    @Where(predicate = Where.Predicate.like)
    String name;

    String desc;

    @Join
    SubProjection subProjection;

    @Join
    Collection<SubProjection> subProjections;


    @SelectFields(fields = {"id", "name"})
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
