package cn.procsl.ping.processor.repository;

import javax.lang.model.element.TypeElement;
import java.util.Collection;

public interface RepositoryNamingStrategy {


    String repositoryName(TypeElement entity, String prefix, Collection<String> repository);

    String repositoryPackageName(TypeElement entity);

}
