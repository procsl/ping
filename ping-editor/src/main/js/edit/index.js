import {EditorState} from "@codemirror/state"
import {basicSetup, EditorView} from "codemirror"
import {vim} from "@replit/codemirror-vim";
import {ViewPlugin} from "@codemirror/view";
import './edit.css'


// 创建一个 ViewPlugin 以监听内容改变事件
const contentChangeListener = ViewPlugin.fromClass(class {

    constructor() {
        this.pub = window.parent?.window?.eventPublish;
    }

    async update(update) {
        if (update.docChanged) {
            // 内容发生改变
            if (this.pub) {
                this.pub(update.state.doc);
            } else {
                console.warn("未绑定推送事件")
            }
        }
    }
});

// 测试
const vimExtend = vim({status: true})
const editorState = EditorState.create({
    doc: "# 我是标题",
    extensions: [vimExtend, basicSetup, contentChangeListener],
});
const doc = document.getElementById("editor-content");
console.log("测试:", doc);

let editorView = new EditorView({
    state: editorState,
    parent: doc
});
console.log("编辑器实例: ", editorView);
// window.parent?.window?.eventPublish();

window.onload = () => {
    // TODO
}
