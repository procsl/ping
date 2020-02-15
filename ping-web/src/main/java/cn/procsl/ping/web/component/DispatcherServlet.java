package cn.procsl.ping.web.component;

import cn.procsl.ping.web.component.exception.NotFoundException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author procsl
 * @date 2019/12/27
 */
@Slf4j
public class DispatcherServlet extends org.springframework.web.servlet.DispatcherServlet {

    @Setter
    protected boolean throwExceptionIfNoHandlerFound = true;

    @Override
    protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (this.throwExceptionIfNoHandlerFound) {
            throw new NotFoundException();
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
