package cn.procsl.ping.boot.user.sys;

import cn.procsl.ping.boot.domain.business.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "u_configure")
@NoArgsConstructor
@AllArgsConstructor
public class Configure extends AbstractPersistable<Long> implements Configuration<Long> {

    String name;

    String configText;

    String description;

}
