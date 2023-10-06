import {EditorState} from "@codemirror/state"
import {basicSetup, EditorView} from "codemirror"
import {unified} from 'unified'
import remarkParse from 'remark-parse'
import {vim} from "@replit/codemirror-vim";
import './main.css'

import remarkGfm from 'remark-gfm'
import rehypeHighlight from 'rehype-highlight'
import rehypeSanitize, {defaultSchema} from 'rehype-sanitize'
import rehypeStringify from 'rehype-stringify'
import remarkRehype from 'remark-rehype'
import {ViewPlugin} from "@codemirror/view";

import hbash from 'highlight.js/lib/languages/bash';
import hdockerfile from 'highlight.js/lib/languages/dockerfile';
import hjavascript from 'highlight.js/lib/languages/javascript';
import hhandlebars from 'highlight.js/lib/languages/handlebars';
import hjava from 'highlight.js/lib/languages/java';
import hjson from 'highlight.js/lib/languages/json';
import hnginx from 'highlight.js/lib/languages/nginx';
import hshell from 'highlight.js/lib/languages/shell'
import hsql from 'highlight.js/lib/languages/sql';
import htypescript from 'highlight.js/lib/languages/typescript';
import hxml from 'highlight.js/lib/languages/xml';
import hyaml from 'highlight.js/lib/languages/yaml';

const processor = unified()
    .use(remarkParse)
    .use(remarkRehype)
    .use(remarkGfm)
    .use(rehypeHighlight, {
        languages: {
            hbash,
            hdockerfile,
            hjavascript,
            hhandlebars,
            hjava,
            hjson,
            hnginx,
            hshell,
            hsql,
            htypescript,
            hxml,
            hyaml
        }
    })
    .use(rehypeSanitize, {
        ...defaultSchema,
        attributes: {
            ...defaultSchema.attributes,
            span: [
                ...(defaultSchema.attributes?.span || []),
                // 这里配置代码块高亮的关键词:
                [
                    'className', 'hljs-addition', 'hljs-attr', 'hljs-attribute', 'hljs-built_in', 'hljs-bullet',
                    'hljs-char', 'hljs-code', 'hljs-comment', 'hljs-deletion', 'hljs-doctag', 'hljs-emphasis',
                    'hljs-formula', 'hljs-keyword', 'hljs-link', 'hljs-literal', 'hljs-meta', 'hljs-name',
                    'hljs-number', 'hljs-operator', 'hljs-params', 'hljs-property', 'hljs-punctuation',
                    'hljs-quote', 'hljs-regexp', 'hljs-section', 'hljs-selector-attr', 'hljs-selector-class',
                    'hljs-selector-id', 'hljs-selector-pseudo', 'hljs-selector-tag', 'hljs-string', 'hljs-strong',
                    'hljs-subst', 'hljs-symbol', 'hljs-tag', 'hljs-template-tag', 'hljs-template-variable',
                    'hljs-title', 'hljs-type', 'hljs-variable'
                ]]
        }
    })
    .use(rehypeStringify)

let markdown = document.querySelector("#markdown-content");

// 创建一个 ViewPlugin 以监听内容改变事件
const contentChangeListener = ViewPlugin.fromClass(class {
    async update(update) {
        if (update.docChanged) {
            // 内容发生改变
            console.log("内容已改变:", update.state.doc.toString());
            let file = await processor.process(update.state.doc.toString())

            let res = String(file);
            console.log("渲染后结果", res);

            markdown.innerHTML = res;
            console.log(res);
        }
    }
});
// 测试
let vimExtend = vim({status: true})
let editorState = EditorState.create({
    doc: "Hello World",
    extensions: [vimExtend, basicSetup, contentChangeListener],
});
let doc = document.querySelector("#editor-content");

let editorView = new EditorView({
    state: editorState,
    parent: doc
});




