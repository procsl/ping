package cn.procsl.ping.processor.model;

final class NoneTypeName implements TypeName {

    @Override
    public String generatorType() {
        return "None";
    }

    @Override
    public String getPackageName() {
        return "";
    }

    @Override
    public String getClassName() {
        return "None";
    }
}
