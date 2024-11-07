package cn.procsl.ping.boot.jpa.support.query.ast.domain;

import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "j_book")
@RepositoryCreator
@NoArgsConstructor
public class Book {


    @Id
    @TableGenerator(name = "ping_sequence")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ping_sequence")
    Long id;

    @Column(length = 100)
    String name;

    String desc;

}
