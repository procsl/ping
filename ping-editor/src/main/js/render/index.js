// document.addEventListener
window.onload = () => {
    const listener = window.parent?.window?.addChannelListener;

    const ele = document.getElementById("render-content");
    console.log("元素", ele);

    if (listener && ele) {
        listener((doc) => onChange(doc, ele));
    } else {
        console.warn("未绑定事件");
    }

}

function onChange(doc, ele) {
    console.log("收到信息了:", doc);
    ele.innerHTML = doc.toString();
}
