package cn.procsl.ping.boot.infra.domain.rbac;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("page")
public class PagePermission extends Permission {

    @NotBlank
    protected String option;

    @NotBlank
    protected String resource;

}
