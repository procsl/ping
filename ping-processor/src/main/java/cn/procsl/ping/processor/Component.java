package cn.procsl.ping.processor;

public interface Component<T, E> {


    /**
     * 组件生成接口
     *
     * @param element 目标元素
     * @return 返回生成的组件
     */
    T generateStruct(E element);

}
