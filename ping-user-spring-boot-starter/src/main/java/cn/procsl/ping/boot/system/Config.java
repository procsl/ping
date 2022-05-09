package cn.procsl.ping.boot.system;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "u_configure")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RepositoryCreator
public class Config extends AbstractPersistable<Long> implements Serializable {

    @UniqueElements
    String key;

    String text;

    @Enumerated(EnumType.STRING)
    ConfigType type;

    String description;

    @Version
    Long version;


    @Builder
    public Config(Long id, String key, String text, ConfigType type, String description, Long version) {
        this.setId(id);
        this.key = key;
        this.text = text;
        this.type = type;
        this.description = description;
        this.version = version;
    }

    @Override
    public void setId(Long id) {
        super.setId(id);
    }
}
