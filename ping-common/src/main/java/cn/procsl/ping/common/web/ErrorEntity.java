package cn.procsl.ping.common.web;

public interface ErrorEntity {

    Integer httpStatus();

    String code();

    String message();

}
