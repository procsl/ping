package cn.procsl.ping.boot.user.sys;


import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

@Indexed
@Service
@RequiredArgsConstructor
public class ConfigureService {

    JpaSpecificationExecutor

    public void createConfigure(String key, String content) {

    }

}
