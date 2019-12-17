package cn.procsl.ping.web.http.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;

/**
 * @author procsl
 * @date 2019/10/07
 */
public abstract class HttpVerticle extends AbstractVerticle {

    protected int port;

    protected Router router;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        this.vertx.createHttpServer()
                .requestHandler(this.router)
                .listen(this.port);
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {

    }

}


