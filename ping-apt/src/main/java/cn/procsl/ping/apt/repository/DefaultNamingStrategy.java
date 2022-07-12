package cn.procsl.ping.apt.repository;

import com.google.auto.service.AutoService;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(RepositoryNamingStrategy.class)
public class DefaultNamingStrategy implements RepositoryNamingStrategy {

    @Override
    public String repositoryName(TypeElement entity, String prefix, Collection<String> repository) {
        Set<String> repositories = repository.stream().filter(item -> !item.isEmpty()).collect(Collectors.toSet());
        String repositoryName = "Repository";
        if (repositories.size() == 1) {
            repositoryName = repositories.iterator().next();
        }

        String name = entity.getSimpleName().toString();

        return prefix + name + repositoryName;
    }

    @Override
    public String repositoryPackageName(TypeElement entity) {
        try {
            Element packageType = entity.getEnclosingElement();
            String last = "." + packageType.getSimpleName().toString();
            return packageType.toString().replace(last, "") + ".repository";
        } catch (Exception e) {
            return "repository";
        }
    }

}
