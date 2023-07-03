package cn.procsl.ping.boot.system.api.menu;

import cn.procsl.ping.boot.system.domain.menu.Menu;
import cn.procsl.ping.boot.web.annotation.SecurityId;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "Menu", description = "菜单接口")
public class MenuController {

    final JpaRepository<Menu, Long> jpaRepository;

    @PostMapping("/v1/menus")
    @Transactional(rollbackFor = Exception.class)
    public void initRootMenu(@RequestBody MenuDTO menu) {
        jpaRepository.save(Menu.createRoot(menu.getName()));
    }

    @PutMapping("/v1/menus/{id}")
    @Transactional(rollbackFor = Exception.class)
    public void createMenu(@PathVariable(name = "id") @SecurityId Long id, @RequestBody MenuDTO menu) {
        Menu child = jpaRepository.getReferenceById(id).createChild(menu.getName());
        jpaRepository.save(child);
    }

}
