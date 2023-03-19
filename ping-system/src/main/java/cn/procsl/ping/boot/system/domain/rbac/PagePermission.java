package cn.procsl.ping.boot.system.domain.rbac;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

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
