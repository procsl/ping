package cn.procsl.ping.demo.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface PathInterfaceMethod {

    @GET
    @Path("/2")
    String get();

}
