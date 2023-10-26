import {vim} from "@replit/codemirror-vim";
import {EditorState} from "@codemirror/state";
import {basicSetup, EditorView} from "codemirror";
import {ViewPlugin} from "@codemirror/view";

const DOCUMENT_CHANGED_LISTENER = "documentChangedListener";

class MarkdownEdit {


    /**
     *
     * @param {function($ObjMap):void} listener
     */
    documentChangedListener(listener) {
        this.listeners[DOCUMENT_CHANGED_LISTENER].push(listener);
    }

    /**
     *
     * @param {string} doc
     * @param {HTMLElement} element
     */
    constructor(doc, element) {
        this.listeners = {};

        this.listeners[DOCUMENT_CHANGED_LISTENER] = [];

        const vimExtend = vim({status: true})
        const _this = this;
        const change = ViewPlugin.fromClass(class {
            async update(update) {
                if (update.docChanged) {
                    const list = _this.listeners[DOCUMENT_CHANGED_LISTENER];
                }
            }
        });

        this.editorState = EditorState.create({
            doc: doc,
            extensions: [vimExtend, basicSetup, change],
        });

        this.editorView = new EditorView({
            state: this.editorState, parent: element
        });
    }

}

export default MarkdownEdit;
