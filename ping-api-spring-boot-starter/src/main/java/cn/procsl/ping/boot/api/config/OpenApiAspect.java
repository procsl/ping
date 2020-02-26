package cn.procsl.ping.boot.api.config;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springdoc.api.OpenApiResource;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author procsl
 * @date 2020/02/25
 */

@Aspect
@RequiredArgsConstructor
public class OpenApiAspect {

    final HttpServletResponse response;

    final OpenApiResource apiResource;

    /**
     * 改造此方法
     * 修改返回值
     */
    @Pointcut("execution(public java.lang.String org.springdoc.api.OpenApiResource.openapiJson(javax.servlet.http.HttpServletRequest," +
            "java.lang.String ))")
    public void openApiJson() {
    }


    /**
     * 修改其返回值
     *
     * @param proceedingJoinPoint
     * @return
     * @throws IOException
     */
    @Around("openApiJson()")
    public String doGround(ProceedingJoinPoint proceedingJoinPoint) throws IOException {
        return null;
    }
}
