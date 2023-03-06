package cn.procsl.ping.momo.client.web;

import cn.procsl.ping.momo.client.service.ActionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class ResultActionVO implements Serializable {

    ActionType action;

    public static ResultActionVO stop() {
        return new ResultActionVO(ActionType.stop);
    }

    public static ResultActionVO start() {
        return new ResultActionVO(ActionType.start);
    }
}
