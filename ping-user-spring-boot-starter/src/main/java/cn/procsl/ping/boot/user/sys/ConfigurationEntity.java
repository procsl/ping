package cn.procsl.ping.boot.user.sys;

import cn.procsl.ping.boot.domain.business.Configuration;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "u_configure")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurationEntity extends AbstractPersistable<Long> implements Configuration {

    String key;

    String configText;

    @Enumerated(EnumType.STRING)
    ConfigurationType type;

    String description;


}
