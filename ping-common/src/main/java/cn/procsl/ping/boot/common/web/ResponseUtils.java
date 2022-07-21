package cn.procsl.ping.boot.common.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public final class ResponseUtils {

    public static void forbiddenError(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String url,
                                      String code, String message) throws ServletException, IOException {
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.FORBIDDEN.value());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        request.setAttribute(RequestDispatcher.ERROR_MESSAGE, message);
        request.getRequestDispatcher(url).forward(request, response);
//        throw new BusinessException(message);
    }

    public static void unauthorizedError(HttpServletRequest request, HttpServletResponse response,
                                         String url,
                                         String code,
                                         String message) throws ServletException, IOException {
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.UNAUTHORIZED.value());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        request.setAttribute(RequestDispatcher.ERROR_MESSAGE, message);
        request.getRequestDispatcher(url).forward(request, response);
//        throw new BusinessException(message);
    }

}
