package cn.procsl.ping.boot.jpa.support.query.ast.domain;

import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "j_teacher")
@RepositoryCreator
@NoArgsConstructor
public class Teacher {

    @Id
    @TableGenerator(name = "ping_sequence")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ping_sequence")
    Long id;

    @Column(length = 100)
    String name;

    @ElementCollection
    Set<Long> bookIds;

    @ManyToMany
    Set<Student> teacher;

    String desc;
}
