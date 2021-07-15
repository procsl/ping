package cn.procsl.ping.web;

public interface ErrorEntity {

    Integer httpStatus();

    String code();

    String message();

}
