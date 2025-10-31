package cn.procsl.ping.boot.mhpp.protocol;

interface CommandHandler {

    boolean handle(Packet packet);

    default void onStarted() {

    }

    default void onPreStop() {

    }

}
