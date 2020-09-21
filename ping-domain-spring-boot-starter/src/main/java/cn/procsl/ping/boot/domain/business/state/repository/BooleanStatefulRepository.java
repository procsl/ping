package cn.procsl.ping.boot.domain.business.state.repository;

import cn.procsl.ping.boot.domain.business.state.model.BooleanStateful;
import lombok.NonNull;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BooleanStatefulRepository<T extends BooleanStateful, ID extends Serializable> {

    /**
     * 禁用指定的实体
     *
     * @param id
     */
    int disable(@NonNull ID id);

    /**
     * 启用指定的实体
     *
     * @param id
     */
    int enable(@NonNull ID id);

    /**
     * 修改状态
     *
     * @param id    指定的ID
     * @param state 状态
     */
    int changeState(@NonNull ID id, @NonNull Boolean state);

    /**
     * 获取指定实体的状态
     *
     * @param id 指定的实体
     * @return 返回实体状态
     */
    Boolean getState(@NonNull ID id);
}
