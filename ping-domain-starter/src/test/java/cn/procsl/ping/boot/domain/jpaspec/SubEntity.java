package cn.procsl.ping.boot.domain.jpaspec;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@Entity
@ToString
@Table(name = "dt_sub")
@NoArgsConstructor
@RepositoryCreator
@AllArgsConstructor
public class SubEntity extends AbstractPersistable<Long> implements Serializable {

    String sub;

    Integer size;

}
