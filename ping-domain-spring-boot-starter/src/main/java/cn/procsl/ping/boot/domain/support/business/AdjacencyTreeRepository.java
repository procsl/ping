package cn.procsl.ping.boot.domain.support.business;

import cn.procsl.ping.boot.domain.business.entity.AdjacencyNode;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 邻接树
 *
 * @author procsl
 * @date 2020/07/31
 */
@NoRepositoryBean
public interface AdjacencyTreeRepository<T extends AdjacencyNode, ID> extends Repository<T, ID> {

    /**
     * 查询父节点列表, 通过指定的id
     *
     * @param id 指定的ID
     * @return 返回父节点Stream
     */
    Stream<T> parents(ID id);

    /**
     * 查询指定节点的所有子节点
     *
     * @param id
     * @return
     */
    Stream<T> children(ID id);

    /**
     * 获取直接父节点
     *
     * @param id 指定的节点ID
     * @return 返回直接父节点
     */
    Optional<T> directParent(ID id);
}
