package cn.procsl.ping.boot.system.domain.ac;

/**
 * 权限裁决结果
 */
public interface Effect {

    String getDescription();

    Effect denied = new Denied();

    Effect allow = new Allow();

    /**
     * 允许
     */
    class Allow implements Effect {
        @Override
        public String getDescription() {
            return "allow";
        }
    }

    /**
     * 拒绝
     */
    class Denied implements Effect {
        @Override
        public String getDescription() {
            return "denied";
        }
    }

}
