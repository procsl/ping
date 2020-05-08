package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.data.annotation.DefaultValue;
import cn.procsl.ping.boot.data.annotation.Description;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author procsl
 * @date 2020/05/09
 */
@Data
@Embeddable
public class PathTree<ID> implements Serializable {

    @Description(comment = "父节点的ID")
    @Column(nullable = false)
    protected ID parentId;

    @Description(comment = "ID路径, 以/分割, 默认为/自身ID")
    @Column(length = 1000, nullable = false)
    protected String fullPath;

    @Description(comment = "深度, 默认深度为0")
    @DefaultValue("0")
    @Column(nullable = false)
    protected Integer depth;
}
