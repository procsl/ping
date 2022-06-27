package cn.procsl.ping.boot.producer;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "p_product")
@NoArgsConstructor
@RepositoryCreator
public class Product extends AbstractPersistable<Long> implements Serializable {

    String name;

}
