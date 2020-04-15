package cn.procsl.ping.boot.data.naming;

import cn.procsl.ping.boot.data.config.DataProperties;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.naming.*;

import java.util.List;

/**
 * @author procsl
 * @date 2019/12/18
 */
@RequiredArgsConstructor
public class NameImplicitNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {

    final DataProperties properties;

    @Override
    public Identifier determineForeignKeyName(ImplicitForeignKeyNameSource source) {
        return this.join(this.properties.getForeignKeyNamePrefix()
                        +
                        this.properties.getDot() + source.getTableName(),
                source.getColumnNames());
    }

    @Override
    public Identifier determineUniqueKeyName(ImplicitUniqueKeyNameSource source) {
        return this.join(this.properties.getUniqueKeyPrefix()
                        +
                        this.properties.getDot() + source.getTableName(),
                source.getColumnNames());
    }

    @Override
    public Identifier determineIndexName(ImplicitIndexNameSource source) {
        return this.join(this.properties.getIndexNamePrefix()
                        +
                        this.properties.getDot() + source.getTableName(),
                source.getColumnNames());
    }

    private Identifier join(String prefix, List<Identifier> source) {
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
