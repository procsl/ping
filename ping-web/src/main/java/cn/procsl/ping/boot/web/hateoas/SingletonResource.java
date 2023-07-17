package cn.procsl.ping.boot.web.hateoas;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nonnull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

import java.util.Arrays;

public class SingletonResource<R> extends EntityModel<R> {


    public SingletonResource(R entity, Iterable<Link> list) {
        super(entity, list);
    }

    @Override
    @Nonnull
    @Schema(implementation = ResourceLink.class, name = "_links")
    public Links getLinks() {
        return super.getLinks();
    }

    public static <R> SingletonResource<R> createResource(R entity, Link... links) {
        return new SingletonResource<>(entity, Arrays.asList(links));
    }

}
