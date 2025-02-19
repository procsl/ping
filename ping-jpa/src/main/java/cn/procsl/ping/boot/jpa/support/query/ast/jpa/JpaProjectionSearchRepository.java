package cn.procsl.ping.boot.jpa.support.query.ast.jpa;

import cn.procsl.ping.boot.jpa.support.query.*;
import cn.procsl.ping.boot.jpa.support.query.ast.Variable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.util.List;
import java.util.Set;

@Slf4j
@Indexed
@RequiredArgsConstructor
@Component("defaultJpaProjectionSearchRepository")
class JpaProjectionSearchRepository implements ProjectionSearchRepository {

    final EntityManager entityManager;

    @Override
    @SuppressWarnings("all")
    public <Q, R> List<R> search(Q query, Class<R> mapping) {

        ProjectionQueryExpression pqe = ProjectionQueryExpression.create(query, mapping);

        String jpql = pqe.toExpString();

        Set<Variable> params = pqe.getQueryVariables();

        TypedQuery<R> eq = entityManager.createQuery(jpql, mapping);
        for (Variable param : params) {
            eq.setParameter(param.getName(), param.getValue());
        }
        log.info("sql语句: \n\n{}\n\n", jpql);

        return eq.getResultList();
    }


}
