package cn.procsl.ping.boot.product.domain;

import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import lombok.*;

import jakarta.persistence.*;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "p_product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RepositoryCreator
public class Product implements Serializable {

    @Id
    @TableGenerator(name = "ping_sequence")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ping_sequence")
    Long id;

    @Column(length = 100)
    String name;

    public Product(String name) {
        this.name = name;
    }
}
