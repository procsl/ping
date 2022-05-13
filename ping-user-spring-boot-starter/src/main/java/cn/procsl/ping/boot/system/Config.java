package cn.procsl.ping.boot.system;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "u_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RepositoryCreator
public class Config extends AbstractPersistable<Long> implements Serializable {

    @Column(unique = true)
    String key;

    String content;

    String description;

    @Version
    Long version;


    @Builder
    public Config(Long id, String key, String content, String description, Long version) {
        this.setId(id);
        this.key = key;
        this.content = content;
        this.description = description;
        this.version = version;
    }

    @Override
    public void setId(Long id) {
        super.setId(id);
    }
}
