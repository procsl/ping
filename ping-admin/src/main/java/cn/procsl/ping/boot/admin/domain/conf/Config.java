package cn.procsl.ping.boot.admin.domain.conf;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "i_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RepositoryCreator
public class Config extends AbstractPersistable<Long> implements Serializable {

    @Column(unique = true)
    String name;

    String content;

    String description;

    @Version
    Long version;


    public static Config creator(@NotNull String key, String content, String description) {
        Config config = new Config();
        config.name = key;
        config.content = content;
        config.description = description;
        return config;
    }

    public void edit(@NonNull String key, String content, String description) {
        this.name = key;
        this.content = content;
        this.description = description;
    }


    @Override
    public void setId(Long id) {
        super.setId(id);
    }

}
