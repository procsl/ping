package cn.procsl.ping.boot.jpa.domain.id;

import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ping_sequence")
@NoArgsConstructor
@AllArgsConstructor
public class Identifier {


    @Id
    @Column(name = "sequence_name")
    String id;


    @Column(nullable = false, name = "next_val")
    Long value;

}
