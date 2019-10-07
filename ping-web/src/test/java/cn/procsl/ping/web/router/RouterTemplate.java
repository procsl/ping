package cn.procsl.ping.web.router;

import cn.procsl.ping.web.http.verticle.HttpVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;

/**
 * @author procsl
 * @date 2019/10/07
 */
public class RouterTemplate extends HttpVerticle {
    @Override
    public void handler(Router router) {
        router.route("").method(HttpMethod.GET).handler(requestHandler -> {
            // 路径参数
            String productType = requestHandler.request().getParam("producttype");

            // 查询参数
            String params = requestHandler.request().getParam("");


        });
    }
}
