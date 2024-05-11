package cn.procsl.ping.boot.system.domain.ac.abac;

import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@RepositoryCreator
@Table(name = "s_abac_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("api")
public class SpelPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ping_sequence")
    Long id;

    String type;

}
