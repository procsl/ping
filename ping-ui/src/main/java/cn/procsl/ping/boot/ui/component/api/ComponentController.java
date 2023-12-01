package cn.procsl.ping.boot.ui.component.api;

import cn.procsl.ping.boot.ui.domain.Component;
import cn.procsl.ping.boot.ui.domain.ComponentProjectionRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "Component", description = "组件列表接口")
public class ComponentController implements InitializingBean {

    final JpaRepository<Component, Long> jpaRepository;

    final ComponentProjectionRepository repository;

    final ComponentMapper componentMapper = Mappers.getMapper(ComponentMapper.class);

    private String ui;


    @GetMapping(value = "/v1/ui/components", produces = APPLICATION_JSON)
    public String queryComponents() {
        return ui;
    }


//    @PutMapping("/v1/ui/components")
//    @Transactional
//    @ResponseStatus(code = HttpStatus.CREATED)
//    public void createComponent(@RequestBody ComponentRecord record) {
//        Component component = this.componentMapper.mapper(record);
//        this.jpaRepository.save(component);
//    }


    @Override
    public void afterPropertiesSet() throws Exception {
        ClassPathResource resource = new ClassPathResource("components/ui.json");
        try (InputStream is = resource.getInputStream()) {
            this.ui = new String(FileCopyUtils.copyToByteArray(is));
        }
    }
}
