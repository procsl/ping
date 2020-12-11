package cn.procsl.ping.demo.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class PathMethod {

    @GET
    @Path("/1")
    public String get() {
        return "root";
    }
}
