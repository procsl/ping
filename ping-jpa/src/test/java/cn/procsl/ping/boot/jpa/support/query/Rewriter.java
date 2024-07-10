package cn.procsl.ping.boot.jpa.support.query;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.QueryRewriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Rewriter implements QueryRewriter {

    @Override
    public String rewrite(String query, Sort sort) {
        log.info("Rewriting query: {}", query);
        return "";
    }

}
