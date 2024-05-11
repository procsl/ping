package cn.procsl.ping.boot.jpa.domain.id;

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
@Table(name = "test_unique")
@NoArgsConstructor
@AllArgsConstructor
public class Unique {

    @Id
    Long id;

    @Column(nullable = false)
    String name;

}
