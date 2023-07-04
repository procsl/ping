package cn.procsl.ping.boot.system.api.menu;

import cn.procsl.ping.boot.system.domain.menu.Menu;
import cn.procsl.ping.boot.web.annotation.SecurityId;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.procsl.ping.boot.jpa.domain.tree.AdjacencyNode.SUPER_ROOT_ID;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "Menu", description = "菜单接口")
public class MenuController {

    final JpaRepository<Menu, Long> jpaRepository;

    final JpaSpecificationExecutor<Menu> menuJpaSpecificationExecutor;

    final EntityManager entityManager;

    @PostMapping("/v1/system/menus")
    @Transactional(rollbackFor = Exception.class)
    public void initRootMenu(@RequestBody MenuDTO menu) {
        Menu entity;
        if (menu.getParentId() == null) {
            entity = Menu.createRoot(menu.getName(), menu.getRouter());
        } else {
            entity = jpaRepository.getReferenceById(menu.getParentId()).createChild(menu.getName(), menu.getRouter());
        }
        jpaRepository.save(entity);
    }

    @GetMapping(path = {"/v1/system/menus"})
    public List<MenuVO> findMenus(@RequestParam(name = "parent_id", required = false) @SecurityId Long parentId) {

        if (parentId == null) {
            parentId = SUPER_ROOT_ID;
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> tupleQuery = builder.createTupleQuery();

        Root<Menu> root = tupleQuery.from(Menu.class);
        Path<String> name = root.get("name");
        Path<String> router = root.get("router");
        Path<Long> id = root.get("id");
        Path<Long> parent = root.get("parentId");
        Path<Integer> depth = root.get("depth");

        CriteriaQuery<Tuple> condition = tupleQuery.multiselect(id, name, parent, depth, router).where(builder.equal(parent, parentId));

        Function<Tuple, MenuVO> converter = item -> new MenuVO(item.get(id), item.get(name), item.get(parent), item.get(router), item.get(depth));
        return entityManager.createQuery(condition).getResultStream().map(converter).collect(Collectors.toList());
    }

}
