class EventChannel {

    constructor() {
        this.eventListener = {};
    }

    /**
     * 推送事件
     * @param {string} key
     * @param {obj|null} obj
     * @return {void}
     */
    publish(key, obj) {
        const list = this.eventListener[key];
        if (list) {
            list.forEach((value) => {
                try {
                    value(obj);
                } catch (error) {
                    console.error("调用事件出现错误:", error);
                }
            });
        }
    }

    /**
     * 推送事件
     * @param {string} key
     * @param {function} func
     */
    addListener(key, func) {
        if (!this.eventListener[key]) {
            this.eventListener[key] = [];
        }
        this.eventListener[key].push(func);
    }

}

export default EventChannel;