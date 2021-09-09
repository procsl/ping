package cn.procsl.ping.processor.restful.model;


import javax.lang.model.element.Name;

public class ParameterNameModel implements Name {

    final String name;


    public ParameterNameModel(String name) {
        this.name = name;
    }

    @Override
    public boolean contentEquals(CharSequence cs) {
        return name.contentEquals(cs);
    }

    @Override
    public int length() {
        return name.length();
    }

    @Override
    public char charAt(int index) {
        return name.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.name.subSequence(start, end);
    }

    @Override
    public String toString() {
        return name;
    }
}
