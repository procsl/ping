package cn.procsl.ping.boot.user;

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

    final AccountFacade accountFacade;

    final AccessControlFacade accessControlFacade;

    final ConfigFacade configFacade;

    final JpaRepository<User, Long> jpaRepository;

    @Transactional
    public void register(@NotNull @RequestBody @Validated RegisterUserDTO userDTO) {
        Long accountId = accountFacade.create(userDTO.getAccount(), userDTO.getPassword());

        User entity = new User();
        BeanUtils.copyProperties(userDTO, entity);
        entity.setAccountId(accountId);
        this.jpaRepository.save(entity);

        final Set<String> defaultRegisterRoleNames = configFacade.getDefaultRoles();
        accessControlFacade.grant(entity.getId(), defaultRegisterRoleNames);
    }


}
