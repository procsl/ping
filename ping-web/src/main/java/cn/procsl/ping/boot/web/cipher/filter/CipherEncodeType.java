package cn.procsl.ping.boot.web.cipher.filter;

public enum CipherEncodeType {

    /**
     * base64编码,未加密
     */
    b64,
    /**
     * 加密未编码
     */
    ebin,
    /**
     * base62编码未加密
     */
    b62,
    /**
     * base64编码且加密
     */
    eb64,
    /**
     * 未加密且未编码
     */
    org

}
