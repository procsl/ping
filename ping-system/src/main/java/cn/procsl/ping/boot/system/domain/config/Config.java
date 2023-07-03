package cn.procsl.ping.boot.system.domain.config;

import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

/**
 * TODO 增加组区分
 */
@Getter
@Setter
@Entity
@Table(name = "s_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RepositoryCreator
public class Config implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    Long id;

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


}
