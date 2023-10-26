import './index.css'
import './tailwind.css'
import EventBridge from "./bridge";

const onDocumentChanged = "onDocumentChanged";
const onDocumentScroll = "onDocumentScroll";
const onPreviewScroll = "onPreviewScroll";

const bridge = new EventBridge();

// 文档修改事件
window.addDocumentChangedListener = (func) => {
    bridge.addListener(onDocumentChanged, func);
}

window.onDocumentChanged = (doc) => {
    bridge.publish(onDocumentChanged, doc);
}

// 文档滚动事件
window.addDocumentScrollListener = (func) => {
    bridge.addListener(onDocumentScroll, func);
}

window.onDocumentScroll = (doc) => {
    bridge.publish(onDocumentScroll, doc);
}

// 预览页面滚动事件
window.addPreviewScrollListener = (func) => {
    bridge.addListener(onPreviewScroll, func);
}

window.onPreviewScroll = (doc) => {
    bridge.publish(onPreviewScroll, doc);
}
