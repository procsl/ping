import {EditorState} from "@codemirror/state"
import {basicSetup, EditorView} from "codemirror"
import {vim} from "@replit/codemirror-vim";
import {ViewPlugin} from "@codemirror/view";
import './edit.css'


// 创建一个 ViewPlugin 以监听内容改变事件
const contentChangeListener = ViewPlugin.fromClass(class {
    async update(update) {
        if (update.docChanged) {
            // 内容发生改变
            console.log("内容已改变:", update.state.doc.toString());
        }
    }
});

// 测试
const vimExtend = vim({status: true})
const editorState = EditorState.create({
    doc: "Hello World",
    extensions: [vimExtend, basicSetup, contentChangeListener],
});
const doc = document.querySelector("#editor-content");
console.log("测试:", doc);

let editorView = new EditorView({
    state: editorState,
    parent: doc
});
console.log("编辑器实例: ", editorView);
window.editorView = editorView;
