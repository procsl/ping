import {EditorState} from "@codemirror/state"
import {basicSetup, EditorView} from "codemirror"

let startState = EditorState.create({
    doc: "Hello World",
    extensions: [basicSetup]
})

let view = new EditorView({
    state: startState,
    parent: document.querySelector("#editor-content")
})