import './main.css'
import EventChannel from "./channel";

const event = new EventChannel();
window.addChannelListener = (func) => {
    event.addListener(func);
}
window.eventPublish = (obj) => {
    event.publish(obj);
}

