package cn.procsl.ping.boot.domain.support.exector;

import cn.procsl.ping.boot.domain.business.entity.AdjacencyNode;
import cn.procsl.ping.boot.domain.support.business.AdjacencyTreeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 邻接树数据库操作
 *
 * @author procsl
 * @date 2020/07/31
 */
@Slf4j
@RequiredArgsConstructor
@NoRepositoryBean
@Transactional(readOnly = true)
public class AdjacencyTreeRepositoryImpl<T extends AdjacencyNode, ID> implements AdjacencyTreeRepository<T, ID> {

    /**
     * 查询当前节点的父节点
     *
     * @param id 指定的ID
     * @return 父节点Stream
     */
    @Override
    public Stream<T> parents(ID id) {
        log.info("call parents");
        return null;
    }

    @Override
    public Stream<T> children(ID id) {
        log.info("call children");
        return null;
    }

    @Override
    public Optional<T> directParent(ID id) {
        log.info("directParent");
        return Optional.empty();
    }
}
