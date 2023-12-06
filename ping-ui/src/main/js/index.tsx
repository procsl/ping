import React from "react";
import {createRoot} from 'react-dom/client';

function MyButton({title}: { title: string }) {
    return (
        <button>{title}</button>
    );
}

export default function MyApp() {
    return (
        <div>
            <h1>欢迎来到我的应用</h1>
            <MyButton title="我是一个按钮"/>
        </div>
    );
}

// 清除现有的 HTML 内容
document.body.innerHTML = '<div id="app"></div>';
let element = document.getElementById('app');
if (element != null) {
// 渲染你的 React 组件
    const root = createRoot(element);
    root.render(<MyApp/>);
}
