package cn.procsl.ping.business.user.impl.config;

import org.hibernate.boot.model.naming.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * @author procsl
 * @date 2019/12/18
 */
public class NameImplicitNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {

    @Value("${ping.business.table.foreignKey.prefix:FK}")
    private String foreignKeyNamePrefix;

    @Value("${ping.business.table.uniqueKey.prefix:UK}")
    private String uniqueKeyPrefix;

    @Value("${ping.business.table.index.prefix:IN}")
    private String indexNamePrefix;

    @Override
    public Identifier determineForeignKeyName(ImplicitForeignKeyNameSource source) {
        return this.join(this.foreignKeyNamePrefix, source.getColumnNames());
    }

    @Override
    public Identifier determineUniqueKeyName(ImplicitUniqueKeyNameSource source) {
        return this.join(this.uniqueKeyPrefix, source.getColumnNames());
    }

    @Override
    public Identifier determineIndexName(ImplicitIndexNameSource source) {
        return this.join(this.indexNamePrefix, source.getColumnNames());
    }

    private Identifier join(String prefix, List<Identifier> source) {
        StringBuilder build = new StringBuilder();
        build.append(prefix);
        source.sort(Identifier::compareTo);
        for (Identifier identifier : source) {
            build.append("_");
            build.append(identifier.getText());
        }
        return Identifier.toIdentifier(build.toString());
    }
}
