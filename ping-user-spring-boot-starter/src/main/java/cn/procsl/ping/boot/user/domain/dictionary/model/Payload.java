package cn.procsl.ping.boot.user.domain.dictionary.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Immutable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@ToString
@Setter
@Getter
@EqualsAndHashCode(exclude = "id")
@Immutable
public class Payload implements Serializable {

    @Column(nullable = false, updatable = false)
    Long id;

    @Column(length = 10, nullable = false, updatable = false)
    String type;

    @Column(length = 500, nullable = false, updatable = false)
    String data;

}
