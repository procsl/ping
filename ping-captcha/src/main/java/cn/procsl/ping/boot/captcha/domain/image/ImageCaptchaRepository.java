package cn.procsl.ping.boot.captcha.domain.image;

import lombok.NonNull;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;

@Indexed
@org.springframework.stereotype.Repository
public interface ImageCaptchaRepository extends Repository<ImageCaptcha, Long> {

    @Transactional(rollbackFor = Exception.class)
    void save(@NonNull ImageCaptcha entity);


    @Transactional(rollbackFor = Exception.class, readOnly = true)
    ImageCaptcha findById(@NonNull Long id);
}
