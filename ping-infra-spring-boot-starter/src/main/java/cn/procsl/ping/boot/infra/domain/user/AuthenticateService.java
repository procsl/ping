package cn.procsl.ping.boot.infra.domain.user;

import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.Optional;

@Indexed
@Service
@Validated
@RequiredArgsConstructor
public class AuthenticateService {

    final JpaSpecificationExecutor<Account> jpaSpecificationExecutor;

    /**
     * @param account  账户名称
     * @param password 账户密码
     * @return 如果登录成功返回账户ID
     * @throws BusinessException 如果登录失败
     */
    @Transactional(readOnly = true)
    public Long authenticate(@NotBlank String account, @NotBlank String password) throws BusinessException {
        Optional<Account> entity = this.jpaSpecificationExecutor.findOne((root, query, cb) -> cb.equal(root.get("name"), account));
        entity.ifPresent(item -> item.authenticate(password));
        return entity.orElseThrow(() -> new BusinessException("账户不存在")).getId();
    }

}
