package cn.procsl.ping.boot.ai.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * 图片输入落盘：按内容哈希命名，同一图片不重复复制。
 *
 * <p>只校验「文件存在且可读」；格式与大小交给模型 provider 判断。</p>
 */
public final class ImageStore {

    private final Path imagesDirectory;

    public ImageStore(Path imagesDirectory) {
        this.imagesDirectory = imagesDirectory;
    }

    /**
     * 落盘一张图片。
     *
     * @param source 源文件
     * @return 落盘结果
     * @throws IOException 读写失败
     * @throws IllegalArgumentException 文件不存在或不可读
     */
    public StoredImage store(Path source) throws IOException {
        if (source == null || !Files.isReadable(source) || !Files.isRegularFile(source)) {
            throw new IllegalArgumentException("图片不可读: " + source);
        }
        byte[] bytes = Files.readAllBytes(source);
        String name = hash(bytes) + extension(source.getFileName().toString());
        Files.createDirectories(imagesDirectory);
        Path target = imagesDirectory.resolve(name);
        if (!Files.exists(target)) {
            Files.write(target, bytes);
        }
        return new StoredImage(target, name, bytes);
    }

    private static String hash(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    private static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot).toLowerCase(Locale.ROOT);
    }

    /**
     * 落盘结果。
     *
     * @param path  落盘路径
     * @param name  文件名
     * @param bytes 内容
     */
    public record StoredImage(Path path, String name, byte[] bytes) {
    }
}
