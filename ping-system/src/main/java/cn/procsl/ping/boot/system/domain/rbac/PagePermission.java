package cn.procsl.ping.boot.system.domain.rbac;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

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
