package cn.procsl.ping.boot.domain.jpaspec;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@Entity
@ToString
@Table(name = "dt_spec")
@NoArgsConstructor
@RepositoryCreator
@AllArgsConstructor
public class JpaSpec extends AbstractPersistable<Long> implements Serializable {

    String name;

    Long max;

    Integer min;

    @OneToOne(cascade = {CascadeType.PERSIST})
    SubEntity subEntity;

    @ElementCollection()
    @CollectionTable(name = "dt_spec_collection")
    Set<String> collections;

}
