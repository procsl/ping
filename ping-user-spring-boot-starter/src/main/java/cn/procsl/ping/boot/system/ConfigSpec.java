package cn.procsl.ping.boot.system;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.validation.constraints.NotNull;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigSpec {

    public static Specification<Config> matchByKey(@NotNull String key) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("key"), key);
    }

    public static Specification<Config> matchById(@NotNull Long id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), id);
    }

    public static <T> Specification<T> matchingByGetter(@NotNull Supplier<Object> getter) {
        String name = getter.getClass().getName().replace("get", "");
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(name), getter.get());
    }

}
