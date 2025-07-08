package cn.procsl.ping.boot.jpa.support.query.ast.jpa;

import cn.procsl.ping.boot.jpa.domain.page.FormatPage;
import cn.procsl.ping.boot.jpa.support.query.ProjectionSearchRepository;
import cn.procsl.ping.boot.jpa.support.query.ast.ResultExtractor;
import cn.procsl.ping.boot.jpa.support.query.ast.Variable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Indexed;

import java.util.List;
import java.util.Set;

@Slf4j
@Indexed
@RequiredArgsConstructor
public class JpaProjectionSearchRepository implements ProjectionSearchRepository {

    final private EntityManager entityManager;

    @Override
    public <Q, R> List<R> search(Q query, Class<R> mapping) {
        ProjectionQueryExpression pqe = ProjectionQueryExpression.create(query, null);
        // 最大3000条
        TypedQuery<R> eq = this.build(pqe, mapping, 0, 3000);
        return eq.getResultList();
    }

    @SuppressWarnings("all")
    private final <Q, R> TypedQuery<R> build(ProjectionQueryExpression pqe, Class<R> mapping, int first, int max) {
        String jpql = pqe.toExpString();
        Set<Variable> params = pqe.getQueryVariables();
        TypedQuery<R> eq = entityManager.createQuery(jpql, mapping);
        for (Variable param : params) {
            eq.setParameter(param.getName(), param.getValue());
        }
        eq.setFirstResult(first);
        eq.setMaxResults(max);
        log.info("sql语句: \n\n{}\n\n", jpql);
        return eq;
    }

    @Override
    @SuppressWarnings("all")
    public <Q, R> FormatPage<R> search(Q query, Class<R> mapping, Pageable pageable) {
        ProjectionQueryExpression pqe = ProjectionQueryExpression.create(query, null);
        int first = (pageable.getPageNumber()) * pageable.getPageSize();
        TypedQuery<R> eq = this.build(pqe, mapping, first, pageable.getPageSize());
        List<R> list = eq.getResultList();
        TypedQuery<Long> total = entityManager.createQuery(pqe.totalExpString(), Long.class);
        PageImpl<R> i = new PageImpl<>(list, pageable, total.getSingleResult());
        return FormatPage.copy(i);
    }

    @Override
    public <Q, R> FormatPage<R> search(Q mapper, ResultExtractor<R> result) {



        return null;
    }


}
