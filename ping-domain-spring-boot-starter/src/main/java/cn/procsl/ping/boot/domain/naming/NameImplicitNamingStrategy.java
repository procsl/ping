package cn.procsl.ping.boot.domain.naming;

import cn.procsl.ping.boot.domain.config.DomainProperties;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.naming.*;

import java.util.List;

/**
 * @author procsl
 * @date 2019/12/18
 */
@RequiredArgsConstructor
public class NameImplicitNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {

    final DomainProperties properties;

    @Override
    public Identifier determineForeignKeyName(ImplicitForeignKeyNameSource source) {
        String prefix = this.properties.getForeignKeyNamePrefix();
        String tableName = source.getTableName() == null ? "" : source.getTableName().getText();
        String dot = prefix.isEmpty() ? "" : prefix;
        return this.join(source.getColumnNames(), prefix, dot, tableName);
    }

    @Override
    public Identifier determineUniqueKeyName(ImplicitUniqueKeyNameSource source) {
        String prefix = this.properties.getUniqueKeyPrefix();
        String tableName = source.getTableName() == null ? "" : source.getTableName().getText();
        String dot = prefix.isEmpty() ? "" : prefix;
        return this.join(source.getColumnNames(), prefix, dot, tableName);
    }

    @Override
    public Identifier determineIndexName(ImplicitIndexNameSource source) {
        String prefix = this.properties.getIndexNamePrefix();
        String tableName = source.getTableName() == null ? "" : source.getTableName().getText();
        String dot = prefix.isEmpty() ? "" : prefix;
        return this.join(source.getColumnNames(), prefix, dot, tableName);
    }

    private Identifier join(List<Identifier> source, String... prefixes) {
        String prefix = String.join("", prefixes);

        StringBuilder build = new StringBuilder();
        build.append(prefix);
        source.sort(Identifier::compareTo);
        for (Identifier identifier : source) {
            build.append(this.properties.getDot());
            build.append(identifier.getText());
        }
        return Identifier.toIdentifier(build.toString());
    }
}
