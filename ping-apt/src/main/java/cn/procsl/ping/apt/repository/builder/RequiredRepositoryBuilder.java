package cn.procsl.ping.apt.repository.builder;

import cn.procsl.ping.apt.repository.EntityAndIdRepositoryBuilder;
import cn.procsl.ping.apt.repository.RepositoryBuilder;
import com.google.auto.service.AutoService;

@AutoService(RepositoryBuilder.class)
public class RequiredRepositoryBuilder extends EntityAndIdRepositoryBuilder {

    @Override
    protected String getSupportRepositoryClass() {
        return "org.springframework.data.repository.Repository";
    }

}
