package cn.procsl.ping.boot.jpa.domain.id;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "ping_sequence")
@NoArgsConstructor
@AllArgsConstructor
class InnerIdentifier implements Serializable, Persistable<String> {


    @Id
    @Column(name = "sequence_name")
    String id;


    @Column(nullable = false, name = "next_val")
    Long value;

    @Override
    public boolean isNew() {
        return true;
    }
}
