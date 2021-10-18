package cn.procsl.ping.processor;

public interface GeneratorDefinition {

    String generatorType();


    /**
     * 获取模块名称, 中文或可读的简短注释
     *
     * @return 模块名称
     */
    String getModuleName();

    /**
     * 获取详细信息描述, 用于描述此文件
     *
     * @return 详细描述
     */
    String getDescription();

}
