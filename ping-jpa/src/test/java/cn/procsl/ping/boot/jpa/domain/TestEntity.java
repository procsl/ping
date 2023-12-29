package cn.procsl.ping.boot.jpa.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "j_test_entity")
@NoArgsConstructor
public class TestEntity {

    @Id
    @TableGenerator(name = "ping_sequence")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ping_sequence")
    Long id;

    @Column(length = 100)
    String name;

    @Column
    Integer age;

    @Embedded
    DomainAuditable auditable;

    public TestEntity(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
}
