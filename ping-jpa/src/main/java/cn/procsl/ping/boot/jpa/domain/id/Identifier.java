package cn.procsl.ping.boot.jpa.support;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ping_sequence")
public class Identifier {


    @Id
    @Column(name = "sequence_name")
    String id;


    @Column(nullable = false, name = "next_val")
    Long value;

}
