package cn.procsl.ping.boot.ui.component;

/**
 * 装配描述中的接口引用经解析后得到的端点信息。
 *
 * @param method HTTP 方法（大写，如 GET）
 * @param path   OpenAPI 文档中的路径模板（如 /v1/system/users）
 */
record ApiEndpoint(String method, String path) {
}
