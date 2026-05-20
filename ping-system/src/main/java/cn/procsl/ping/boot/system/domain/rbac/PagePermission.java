package cn.procsl.ping.boot.system.domain.rbac;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("page")
public class PagePermission extends Permission {

    @Id
    @GeneratedValue
    Long id;

    @NotBlank
    protected String operate;

    @NotBlank
    protected String resource;

    public PagePermission(String operate, String resource) {
        this.operate = operate;
        this.resource = resource;
    }
}
