package cn.procsl.ping.demo;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

/**
 * @author procsl
 * @date 2019/10/07
 */
public class Application {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        HttpServer httpService = vertx.createHttpServer();
        httpService.requestHandler(Application.handler(vertx)).listen(8888);
    }

    private static Router handler(Vertx vertx) {
        Router router = Router.router(vertx);

        router.route("/echo").handler(context -> {
            String input = context.request().getParam("input");
            context.response()
                    .putHeader("content-type", "text/plain; charset=UTF-8")
                    .end(input);
        }).method(HttpMethod.GET);

        router.route("/path/:input").handler(context -> {
            String input = context.request().getParam("input");
            context.response()
                    .putHeader("content-type", "text/plain; charset=UTF-8")
                    .end(input);
        }).method(HttpMethod.GET);

        return router;
    }
}
