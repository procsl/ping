package cn.procsl.ping.boot.admin.auth.login;

import cn.procsl.ping.boot.admin.domain.session.Session;
import cn.procsl.ping.boot.admin.domain.session.SessionSpecification;
import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.common.dto.MessageVO;
import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.event.Publisher;
import cn.procsl.ping.boot.common.web.Created;
import cn.procsl.ping.boot.common.web.Deleted;
import cn.procsl.ping.boot.common.web.Query;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Indexed;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static cn.procsl.ping.boot.admin.constant.EventPublisherConstant.USER_LOGIN;
import static cn.procsl.ping.boot.admin.constant.EventPublisherConstant.USER_LOGOUT;


@Slf4j
@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "session", description = "用户会话信息管理")
@RestControllerAdvice
public class SessionController {

    final JpaSpecificationExecutor<Session> jpaSpecificationExecutor;

    @Query(path = "/v1/admin/session", summary = "获取用户当前登录信息")
    public SessionUserDetail currentSession(HttpServletRequest request) {

        Optional optional = this.jpaSpecificationExecutor.findOne(
                new SessionSpecification(request.getRequestedSessionId()));
        throw new BusinessException(HttpStatus.UNAUTHORIZED, "401002", "尚未登录,请登录");
    }

    @PermitAll
    @VerifyCaptcha(type = CaptchaType.image)
    @Publisher(name = USER_LOGIN, parameter = "#details.account")
    @Created(path = "/v1/admin/session", summary = "用户登录")
    public SessionUserDetail authenticate(HttpServletRequest request, HttpServletResponse response,
                                          @Validated @RequestBody LoginDetailDTO details)
            throws ServletException, IOException {

        return null;
    }

    @Publisher(name = USER_LOGOUT, parameter = "#root[currentAccount].get()?.id")
    @Deleted(path = "/v1/admin/session", summary = "用户注销")
    public MessageVO deleteSession(HttpServletRequest request, HttpServletResponse response) {

        return null;
    }

}
