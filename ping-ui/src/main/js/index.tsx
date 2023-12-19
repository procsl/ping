import React from "react";
import {createRoot} from 'react-dom/client';
import MainContainer from "./ui/antd/MainContainer";

export default function Application() {
    return (
        <div>
            <h1>欢迎来到我的应用</h1>
            <MainContainer/>
        </div>
    );
}

// 清除现有的 HTML 内容
document.body.innerHTML = '<div id="app"></div>';
let element = document.getElementById('app');
if (element != null) {
// 渲染你的 React 组件
    const root = createRoot(element);
    root.render(<Application/>);
}
