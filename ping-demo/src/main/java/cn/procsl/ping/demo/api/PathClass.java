package cn.procsl.ping.demo.api;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("root")
@Slf4j
public class PathClass {

    @POST
    public String post(@QueryParam("q") String string) {
        log.info("test");
        return string;
    }

    @Path("1")
    @POST
    public String post1(String string, int code, @QueryParam("status") int status) {
        log.info("test");
        return string;
    }

    @GET
    @Path("1")
    public String get() {
        return "root";
    }
}
