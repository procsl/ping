package cn.procsl.ping.boot.domain.valid;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "dt_unique")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RepositoryCreator
@AllArgsConstructor
@UniqueField(entity = Unique.class, fieldName = "column", useSpringEntityManager = false)
public class Unique extends AbstractPersistable<Long> implements Serializable {

    @Column(unique = true, nullable = false, length = 10)
    String column;

    @ElementCollection
    Set<String> size;

    public Unique(String column) {
        this.column = column;
    }

    public Unique(Long id, String column) {
        this.setId(id);
        this.column = column;
    }

}
