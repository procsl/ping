package cn.procsl.ping.boot.domain.valid;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.*;
import org.springframework.data.domain.Persistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Getter
@Setter
@Entity
@javax.persistence.Table(name = "dt_table")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RepositoryCreator
@AllArgsConstructor
public class Table implements Persistable<Long>, Serializable {

    @Id
    Long id;

    @Column(unique = true, nullable = false, length = 20)
    String uniqueKey;

    @Override
    public boolean isNew() {
        return id == null;
    }
}
