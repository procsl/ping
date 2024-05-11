package cn.procsl.ping.boot.system.domain.ac.abac;

import cn.procsl.ping.boot.jpa.support.DiscriminatorValueFinder;
import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import cn.procsl.ping.boot.system.domain.ac.Effect;
import cn.procsl.ping.boot.system.domain.ac.Resource;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.expression.ExpressionParser;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@Entity
@RepositoryCreator
@Table(name = "s_abac_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("restful")
public class RestfulPolicy implements Serializable, DiscriminatorValueFinder, Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ping_sequence")
    Long id;


    @Column(nullable = false)
    String condition;

    @Override
    public Effect test(ExpressionParser parser,
                       Map<String, Object> rootContext,
                       AttributeRequest request,
                       Resource resource) {

//        if (resource.getObject())

        if (this.isNotWebContext(request)) {
            return null;
        }

        if (!this.hasNeedRestfulAccessControl(request)) {
            return null;
        }


        return null;
    }

    /**
     * 是否需要权限校验
     */
    private boolean hasNeedRestfulAccessControl(AttributeRequest request) {
        return false;
    }


    /**
     * 是否是web环境
     */
    boolean isNotWebContext(AttributeRequest request) {
        return false;
    }


}
