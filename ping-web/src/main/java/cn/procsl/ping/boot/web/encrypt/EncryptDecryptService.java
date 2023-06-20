package cn.procsl.ping.boot.web.encrypt;

/**
 * 可以使用对象池的技术缓存一些加密器解密器, 防止每个请求重新创建
 */
public interface EncryptDecryptService {

    String encryptByContext(Long id);

    Long decryptByContext(String code) throws DecryptException;

}