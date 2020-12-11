package cn.procsl.ping.demo.api;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("root")
public interface PathInterface {

    @POST
    String post() throws Exception;
}
