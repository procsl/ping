package cn.procsl.ping.boot.system.domain.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigTest {

    @Test
    void creator() {
        Config config = Config.creator("key", "context", "desc");
        Assertions.assertNotNull(config);
        Assertions.assertEquals("key", config.getName());
        Assertions.assertEquals("context", config.getContent());
        Assertions.assertEquals("desc", config.getDescription());
    }

    @Test
    void edit() {
        Config config = Config.creator("key", "context", "desc");
        Assertions.assertNotNull(config);
        Assertions.assertEquals("key", config.getName());
        Assertions.assertEquals("context", config.getContent());
        Assertions.assertEquals("desc", config.getDescription());

        config.edit("key2", "test", null);
        Assertions.assertEquals("key2", config.getName());
        Assertions.assertEquals("test", config.getContent());
        Assertions.assertNull(config.getDescription());

        config.edit("key2", null, null);
        Assertions.assertNull(config.getContent());

    }
}