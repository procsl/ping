package cn.procsl.ping.boot.system.domain.menu;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

import static cn.procsl.ping.boot.jpa.domain.tree.AdjacencyNode.SUPER_ROOT_ID;

public class MenuSpec implements Specification<Menu> {

    final Long parentId;

    public MenuSpec(Long parentId) {
        this.parentId = Objects.requireNonNullElse(parentId, SUPER_ROOT_ID);
    }

    @Override
    public Predicate toPredicate(Root<Menu> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Path<Long> parent = root.get("parentId");
        return query.where(cb.equal(parent, parentId)).getRestriction();
    }

}
