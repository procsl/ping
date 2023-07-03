package cn.procsl.ping.boot.system.api.user;

import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.common.dto.MessageVO;
import cn.procsl.ping.boot.common.event.Publisher;
import cn.procsl.ping.boot.system.domain.user.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static cn.procsl.ping.boot.system.constant.EventPublisherConstant.USER_LOGIN;


@Slf4j
@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "Authenticate", description = "用户认证信息管理")
@RestControllerAdvice
public class AuthenticateController {

    public static final String AUTHENTICATION_KEY = "cn.procsl.ping.system.user.authenticate.key";

    final JpaSpecificationExecutor<Authenticate> jpaSpecificationExecutor;
    final JpaRepository<Authenticate, Long> authenticationLongJpaRepository;

    final JpaSpecificationExecutor<User> userJpaSpecificationExecutor;

    final AuthenticateMapper authenticateMapper = Mappers.getMapper(AuthenticateMapper.class);

    final AuthenticateService handler = new AuthenticateService();

    @Operation(summary = "获取用户当前登录信息")
    @GetMapping(path = "/v1/system/authentications/current")
    @ResponseStatus(HttpStatus.OK)
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public AuthenticateVO currentAuthentication(HttpServletRequest request) {
        Authenticate authenticate = this.findCurrentAuthentication(request);
        return authenticateMapper.mapper(authenticate);
    }

    Authenticate findCurrentAuthentication(HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            throw new AuthenticateException("尚未登录,请登录");
        }
        Long id = (Long) httpSession.getAttribute(AUTHENTICATION_KEY);
        if (id == null) {
            throw new AuthenticateException("尚未登录,请登录");
        }
        return this.authenticationLongJpaRepository.getReferenceById(id);
    }

    @PermitAll
    @VerifyCaptcha(type = CaptchaType.image)
    @Publisher(eventName = USER_LOGIN, parameter = "#details.account")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping(path = "/v1/system/authentications")
    @Operation(summary = "用户登录")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthenticateVO createAuthentication(HttpServletRequest request, HttpServletResponse response, @Validated @RequestBody AuthenticateDTO details) throws AuthenticateException {

        Object auth = request.getSession().getAttribute(AUTHENTICATION_KEY);
        if (auth != null) {
            throw new AuthenticateException("用户已登录, 请先注销登录");
//            this.deleteSession(request, response);
        }
        Optional<User> optional = this.userJpaSpecificationExecutor.findOne(new UserSpec(details.getAccount()));

        optional.orElseThrow(() -> new AuthenticateException("用户名或密码错误"));
        Authenticate authenticate = this.handler.doLogin(request.getSession().getId(), details.getPassword(), optional.get());

        this.authenticationLongJpaRepository.save(authenticate);

        AuthenticateVO dto = this.authenticateMapper.mapper(authenticate);
        request.getSession().setAttribute(AUTHENTICATION_KEY, authenticate.getId());
        return dto;
    }

    //    @Publisher(name = USER_LOGOUT, parameter = "#root[currentAccount].get()?.id")
    @Operation(summary = "用户注销")
    @DeleteMapping(path = "/v1/system/authentications/current")
    @ResponseStatus(HttpStatus.OK)
    @Transactional(rollbackFor = Exception.class)
    public MessageVO deleteAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            Authenticate authenticate = this.findCurrentAuthentication(request);
            authenticate.offline();
        } catch (Exception e) {
            log.warn("注销失败", e);
        } finally {
            request.getSession().invalidate();
        }
        return new MessageVO("用户已成功注销登录");
    }

}
