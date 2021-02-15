package cn.procsl.ping.demo.api;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;

@Path("example")
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.MODULE)
@Controller
@Builder
@Tag(name = "标签", externalDocs = @ExternalDocumentation(url = "https://procsl.cn/api-docs/index.html?q=1"))
public class ExampleService {


    @POST
    public String post(@QueryParam("") String query) {
        log.info("test");
        return query;
    }

    @Path("test/{code}")
    @PUT
    public String put(String userId, @PathParam("code") int code, @QueryParam("") int status, long content) {
        log.info("test");
        return userId;
    }

    @GET
    @Path("1")
    public String get() {
        return "root";
    }

}
