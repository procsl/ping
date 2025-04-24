package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.jpa.domain.page.FormatPage;
import cn.procsl.ping.boot.jpa.support.query.ProjectionSearchRepository;
import cn.procsl.ping.boot.web.annotation.MarkPageable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "用户管理模块接口")
public class UserQueryController {

    final ProjectionSearchRepository searchRepository;

    final EntityManager entityManager;

    @MarkPageable
    @Operation(summary = "获取用户列表")
    @GetMapping("/v1/system/users")
    public FormatPage<UserRecord> findUsers(Pageable pageable, @ModelAttribute UserQuery query) {
//        entityManager.createQuery("select u,u.account.name, u.account.state from User as u");
        return this.searchRepository.search(query, UserRecord.class, pageable);
    }

}
