package cn.procsl.ping.boot.system.constant;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@Setter
@Getter
@ConfigurationProperties(prefix = "procsl.ping.boot.system")
public class SystemConfigureProperties implements Serializable {

    String[] authenticatesPrefix = new String[]{"cn.procsl.ping"};

}
