package cn.procsl.ping.boot.jpa.domain.id;

import cn.procsl.ping.boot.common.utils.IdentifierGenerator;
import jakarta.annotation.Nonnull;

public final class IdentifierGeneratorWrapper implements IdentifierGenerator<Long> {

    final private SegmentIdentifierGenerator generator;
    final private String name;

    @Override
    public Long nextId() {
        return generator.nextId(name);
    }

    public IdentifierGeneratorWrapper(@Nonnull SegmentIdentifierGenerator generator, @Nonnull String name) {
        this.generator = generator;
        this.name = name;
    }
}
