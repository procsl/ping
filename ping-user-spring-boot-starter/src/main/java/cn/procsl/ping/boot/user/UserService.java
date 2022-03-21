package cn.procsl.ping.boot.user;

import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Indexed
@Service
@RequiredArgsConstructor
public class UserService {

    final JpaRepository<UserEntity, Long> jpaRepository;

    public Long register(@NotNull User user) throws BusinessException {
        UserEntity entity = new UserEntity();
        BeanUtils.copyProperties(user, entity);
        this.jpaRepository.save(entity);
        return entity.getId();
    }

}
