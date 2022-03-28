package cn.procsl.ping.boot.user;

import cn.procsl.ping.boot.account.AccountEntity;
import cn.procsl.ping.boot.account.AccountService;
import cn.procsl.ping.boot.rbac.AccessControlService;
import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Indexed
@Service
@RequiredArgsConstructor
public class UserService {

    final AccountService accountService;

    final AccessControlService accessControlService;

    final Set<String> defaultRegisterRoleNames;

    final JpaRepository<UserEntity, Long> jpaRepository;

    protected Long create(@NotNull User user) throws BusinessException {
        UserEntity entity = new UserEntity();
        BeanUtils.copyProperties(user, entity);
        this.jpaRepository.save(entity);
        return entity.getId();
    }

    @Transactional
    public void register(@NotNull @RequestBody @Validated RegisterUserDTO userDTO) {
        Long userId = this.create(userDTO);
        AccountEntity account = AccountEntity.builder().name(userDTO.getAccount()).password(userDTO.getPassword()).userId(userId).build();
        Long accountId = accountService.create(account);
        accessControlService.grant(accountId, defaultRegisterRoleNames);
    }


}
