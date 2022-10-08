package cn.procsl.ping.boot.oa.domain.depart;

import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@ToString
@RepositoryCreator
@AllArgsConstructor
@Table(name = "o_department", uniqueConstraints = @UniqueConstraint(name = "unique_department_name", columnNames =
        "name")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department extends AbstractPersistable<Long> implements Serializable {

    @Column(updatable = false, length = 15)
    String name;


    @ElementCollection
    @CollectionTable(name = "o_department_employee")
    Set<String> employeeId;

    public Department(String name, Collection<String> employeeId) {
        this.name = name;
        this.employeeId = new HashSet<>(employeeId.size());
        this.addEmployee(employeeId);
    }

    public void addEmployee(Collection<String> employeeId) {
        this.employeeId.addAll(employeeId);
    }

    public void removeEmployee(String employeeId) {
        this.employeeId.remove(employeeId);
    }

}
