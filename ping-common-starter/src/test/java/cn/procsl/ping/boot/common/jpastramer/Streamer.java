package cn.procsl.ping.boot.common.jpastramer;


import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Collection;

@Getter
@Setter
@Entity
@ToString
@Table(name = "dt_streamer")
@NoArgsConstructor
@RepositoryCreator
@AllArgsConstructor
public class Streamer extends AbstractPersistable<Long> implements Serializable {

    String name;

    Long number;

    Types type;

    @ElementCollection()
    @CollectionTable(name = "dt_streamer_collection")
    Collection<String> collection;

}
