package cn.procsl.ping.demo.api;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("user")
public interface UserService {

    @Path("{account}")
    @POST
    String createNewUser(String account, String password, String name);

}
