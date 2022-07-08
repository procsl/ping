package cn.procsl.ping.boot.common.web;

public interface ErrorEntity {

    Integer httpStatus();

    String code();

    String message();

}
