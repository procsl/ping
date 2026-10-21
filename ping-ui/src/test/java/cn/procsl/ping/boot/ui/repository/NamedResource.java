package cn.procsl.ping.boot.ui.repository;

import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * 命名内存资源：单测注入多资源，覆盖跨文件合并行为（classpath 扫描本身由真实测试资源覆盖）。
 */
class NamedResource extends ByteArrayResource {

    private final String name;

    NamedResource(String name, byte[] content) {
        super(content);
        this.name = name;
    }

    static NamedResource of(String name, String content) {
        return new NamedResource(name, content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getFilename() {
        return name;
    }

    @Override
    public URI getURI() throws IOException {
        return URI.create("file:/test/" + name);
    }
}
