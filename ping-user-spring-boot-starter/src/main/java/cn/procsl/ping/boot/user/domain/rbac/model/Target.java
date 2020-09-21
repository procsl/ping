package cn.procsl.ping.boot.user.domain.rbac.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)// for jpa
@AllArgsConstructor
public class Target implements Serializable {
    String name;
}
