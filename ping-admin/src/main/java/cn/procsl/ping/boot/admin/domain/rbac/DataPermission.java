package cn.procsl.ping.boot.admin.domain.rbac;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.ObjectUtils;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * 数据权限
 */
@Data
@Getter
@Setter
@Entity
@NoArgsConstructor
@DiscriminatorValue("data")
public class DataPermission extends Permission {

    String operate;

    String resource;

    @Override
    public boolean readOnly() {
        return true;
    }

    public DataPermission(String operate, String resource) {
        this.operate = operate;
        this.resource = resource;
        this.summary = operate + resource;
    }

    public boolean matcher(String option) {
        return ObjectUtils.nullSafeEquals(option, this.operate + resource);
    }
}
