package cn.procsl.ping.boot.product.domain;

import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;

import static jakarta.persistence.GenerationType.TABLE;

@Getter
@Setter
@Entity
@Table(name = "p_product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RepositoryCreator
public class Product implements Serializable {

    @Id
    @TableGenerator(name = "id_gen", table = "id_gen", initialValue = 10)
    @GeneratedValue(strategy = TABLE, generator = "id_gen")
    Long id;

    @Column(length = 100)
    String name;

    public Product(String name) {
        this.name = name;
    }
}
