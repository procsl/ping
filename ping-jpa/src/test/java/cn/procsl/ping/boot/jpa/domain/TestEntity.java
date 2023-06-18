package cn.procsl.ping.boot.jpa.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "j_test_entity")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestEntity {

    @Id
    @TableGenerator(name = "table_sequence")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "table_sequence")
    Long id;

    @Column(length = 100)
    String name;

}
