package cn.procsl.ping.boot.jpa.support.query;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "j_sub_entity")
@NoArgsConstructor
public class SubEntity {


    @Id
    @TableGenerator(name = "ping_sequence")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ping_sequence")
    Long id;

    Long mainId;

    @Column(length = 100)
    String name;


    String desc;

    public SubEntity(String name) {
        this.name = name;
    }
}
