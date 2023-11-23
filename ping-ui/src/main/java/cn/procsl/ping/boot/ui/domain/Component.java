package cn.procsl.ping.boot.ui.domain;

import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Table(name = "ui_component")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RepositoryCreator
public class Component implements Serializable {

    @Id
    @Access(AccessType.PROPERTY)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ping_sequence")
    Long id;

    @Column(nullable = false, length = 40)
    String name;

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "id"), name = "ui_component_tags")
    Set<String> tags;

}
