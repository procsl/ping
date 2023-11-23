package cn.procsl.ping.boot.ui.component.api;

import cn.procsl.ping.boot.jpa.domain.page.FormatPage;
import cn.procsl.ping.boot.ui.domain.Component;
import cn.procsl.ping.boot.ui.domain.ComponentProjectionRepository;
import cn.procsl.ping.boot.web.annotation.MarkPageable;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "Component", description = "组件列表接口")
public class ComponentController {

    final JpaRepository<Component, Long> jpaRepository;

    final ComponentProjectionRepository repository;

    final ComponentMapper componentMapper = Mappers.getMapper(ComponentMapper.class);

    @GetMapping("/v1/ui/components")
    @MarkPageable
    @Transactional(readOnly = true)
    public FormatPage<ComponentRecord> queryComponents(Pageable pageable) {
        Page<ComponentRecord> result = this.repository.findAll(null, ComponentRecord.class, pageable);
        return FormatPage.copy(result);
    }


    @PutMapping("/v1/ui/components")
    @Transactional
    @ResponseStatus(code = HttpStatus.CREATED)
    public void createComponent(@RequestBody ComponentRecord record) {
        Component component = this.componentMapper.mapper(record);
        this.jpaRepository.save(component);
    }


}
