class EventChannel {

    constructor() {
        this.eventListener = [];
    }

    /**
     * 推送事件
     * @param obj
     */
    publish(obj) {
        try {
            this.eventListener.forEach((value, index, array) => value(obj));
        } catch (error) {
            console.error("调用事件出现错误:", error);
        }
    }

    /**
     * 添加监听器
     * @param func
     */
    addListener(func) {
        this.eventListener.push(func);
    }

}

export default EventChannel;