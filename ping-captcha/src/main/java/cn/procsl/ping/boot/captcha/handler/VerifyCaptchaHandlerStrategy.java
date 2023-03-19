package cn.procsl.ping.boot.captcha.handler;

import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptchaRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.EnumMap;
import java.util.function.Function;

@Indexed
@Service
@RequiredArgsConstructor
public class VerifyCaptchaHandlerStrategy implements InitializingBean {

    final ImageCaptchaRepository imageCaptchaRepository;

    final EmailCaptchaHandler emailCaptchaHandler;

    final EnumMap<CaptchaType, VerifyCaptchaHandler<VerifyCaptchaCommand>> strategy = new EnumMap<>(CaptchaType.class);

    final EnumMap<CaptchaType, Function<InnerContext, VerifyCaptchaCommand>> contexts = new EnumMap<>(
            CaptchaType.class);

    protected VerifyCaptchaCommand emailContextBuild(InnerContext innerContext) {
        return null;
    }

    protected VerifyCaptchaCommand imageContextBuild(InnerContext innerContext) {
        return new ImageVerifyCommand(innerContext.request);
    }

    public void verifyForStrategy(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                  @NonNull HandlerMethod handler, VerifyCaptcha verifyCaptcha) {

        VerifyCaptchaCommand context = contexts.get(verifyCaptcha.type())
                                               .apply(new InnerContext(request, response, handler, verifyCaptcha));

        strategy.get(verifyCaptcha.type()).verify(context);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        {
            contexts.put(CaptchaType.image, this::imageContextBuild);
            ImageVerifyCaptchaHandler image = new ImageVerifyCaptchaHandler(imageCaptchaRepository);
            strategy.put(CaptchaType.image, image);
        }

        {
            contexts.put(CaptchaType.email, this::emailContextBuild);
            strategy.put(CaptchaType.email, emailCaptchaHandler);
        }
    }

    @RequiredArgsConstructor
    protected static final class InnerContext {
        final HttpServletRequest request;
        final HttpServletResponse response;
        final HandlerMethod handler;
        final VerifyCaptcha verifyCaptcha;
    }

}
