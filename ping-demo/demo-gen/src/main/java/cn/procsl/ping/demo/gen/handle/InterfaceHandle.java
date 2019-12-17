package cn.procsl.ping.demo.gen.handle;

import cn.procsl.ping.web.http.annotation.Get;
import cn.procsl.ping.web.http.annotation.Post;

/**
 * @author procsl
 * @date 2019/10/13
 */
public interface InterfaceHandle {

    @Get
    void inter();

    @Post
    String test();
}
