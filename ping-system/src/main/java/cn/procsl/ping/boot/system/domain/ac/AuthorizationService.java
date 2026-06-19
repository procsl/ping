package cn.procsl.ping.boot.system.domain.ac;


/**
 * 访问控制服务
 */
public interface AuthorizationService {

    /**
     * 核心无状态纯函数：f(S, A, R, E) -> Effect
     * * @param s 主体属性集
     * @param a 操作属性集
     * @param r 资源属性集
     * @param e 环境属性集
     * @return 包含决策和副作用指令的响应对象
     */
    Effect evaluate(Subject s, Action a, Resource r, Environment e);

}
