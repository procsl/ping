package cn.procsl.ping.boot.user.domain.rbac.service;


import cn.procsl.ping.boot.user.domain.rbac.model.Target;
import lombok.Value;

@Value
public class TargetValue implements Target {
    String resource;

    String operator;
}
