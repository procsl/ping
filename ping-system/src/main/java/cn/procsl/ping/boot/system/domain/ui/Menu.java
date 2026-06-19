package cn.procsl.ping.boot.system.domain.ui;


import lombok.Getter;

@Getter
public class Menu implements Component {

    Long id;


    @Override
    public String getType() {
        return "system-menu";
    }

}
