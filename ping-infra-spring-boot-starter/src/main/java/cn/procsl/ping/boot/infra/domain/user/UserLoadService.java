package cn.procsl.ping.boot.infra.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Indexed
@Service
@RequiredArgsConstructor
public class UserLoadService {

    final JpaSpecificationExecutor<User> jpaSpecificationExecutor;

    @Transactional(readOnly = true)
    public Optional<User> loadByAccount(String account) {
        return jpaSpecificationExecutor.findOne(new UserSpecification(account));
    }

}
