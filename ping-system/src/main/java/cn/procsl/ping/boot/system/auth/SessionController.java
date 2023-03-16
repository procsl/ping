package cn.procsl.ping.boot.system.auth;

import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.common.dto.MessageVO;
import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.event.Publisher;
import cn.procsl.ping.boot.system.domain.session.Session;
import cn.procsl.ping.boot.system.domain.session.UserLoginService;
import cn.procsl.ping.boot.system.domain.user.AuthenticateException;
import cn.procsl.ping.boot.system.domain.user.User;
import cn.procsl.ping.boot.system.domain.user.UserSpecification;
import cn.procsl.ping.boot.web.Created;
import cn.procsl.ping.boot.web.Deleted;
import cn.procsl.ping.boot.web.Query;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Optional;

import static cn.procsl.ping.boot.system.constant.EventPublisherConstant.USER_LOGIN;


@Slf4j
@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "session", description = "用户会话信息管理")
@RestControllerAdvice
public class SessionController {

    public static final String session_key = "cn.procsl.ping.system.user.session.key";

    final JpaSpecificationExecutor<Session> jpaSpecificationExecutor;
    final JpaRepository<Session, Long> sessionLongJpaRepository;

    final JpaSpecificationExecutor<User> userJpaSpecificationExecutor;

    final MapStructMapper mapStructMapper = Mappers.getMapper(MapStructMapper.class);

    final UserLoginService handler = new UserLoginService();

    @Query(path = "/v1/system/session", summary = "获取用户当前登录信息")
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public SessionUserDetail currentSession(HttpServletRequest request) {
        Session session = this.findCurrentSession(request);
        return mapStructMapper.mapper(session);
    }

    Session findCurrentSession(HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "002", "尚未登录,请登录");
        }
        Long id = (Long) httpSession.getAttribute(session_key);
        if (id == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "002", "尚未登录,请登录");
        }
        return this.sessionLongJpaRepository.getReferenceById(id);
    }

    @PermitAll
    @VerifyCaptcha(type = CaptchaType.image)
    @Publisher(name = USER_LOGIN, parameter = "#details.account")
    @Created(path = "/v1/system/session", summary = "用户登录")
    @Transactional(rollbackFor = Exception.class)
    public SessionUserDetail authenticate(HttpServletRequest request, HttpServletResponse response,
                                          @Validated @RequestBody LoginDetailDTO details)
            throws AuthenticateException {

        Object auth = request.getSession().getAttribute(session_key);
        if (auth != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "002", "用户已登录, 请先注销登录");
//            this.deleteSession(request, response);
        }
        Optional<User> optional = this.userJpaSpecificationExecutor.findOne(
                new UserSpecification(details.getAccount()));

        optional.orElseThrow(() -> new AuthenticateException("用户名或密码错误"));
        Session session = this.handler.doLogin(request.getSession().getId(), details.getPassword(), optional.get());

        this.sessionLongJpaRepository.save(session);

        SessionUserDetail dto = this.mapStructMapper.mapper(session);
        request.getSession().setAttribute(session_key, session.getId());
        return dto;
    }

    //    @Publisher(name = USER_LOGOUT, parameter = "#root[currentAccount].get()?.id")
    @Deleted(path = "/v1/system/session", summary = "用户注销")
    @Transactional(rollbackFor = Exception.class)
    public MessageVO deleteSession(HttpServletRequest request, HttpServletResponse response) {
        try {
            Session session = this.findCurrentSession(request);
            session.logout();
        } catch (Exception ignore) {
            log.warn("注销失败 ", ignore);
        } finally {
            request.getSession().invalidate();
        }
        return new MessageVO("用户已成功注销登录");
    }

}