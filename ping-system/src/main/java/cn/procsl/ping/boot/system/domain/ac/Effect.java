package cn.procsl.ping.boot.system.domain.ac;

/**
 * 权限裁决结果
 */
public interface Result {

    String getDescription();

    /**
     * 允许
     */
    class Allow implements Result {
        @Override
        public String getDescription() {
            return "allow";
        }
    }

    /**
     * 拒绝
     */
    class Denied implements Result {
        @Override
        public String getDescription() {
            return "denied";
        }
    }

}
