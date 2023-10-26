import remarkParse from 'remark-parse'
import remarkRehype from 'remark-rehype'
import rehypeStringify from 'rehype-stringify'
import {unified} from 'unified'
import remarkGfm from "remark-gfm";
import remarkBreaks from "remark-breaks";
import remarkMath from "remark-math";
import './github.css';
import rehypeMathjax from 'rehype-mathjax'
import {toc} from "@jsdevtools/rehype-toc";
import slug from "rehype-slug";
import rehypeMermaid from 'rehype-mermaid';
import rehypeHighlight from "rehype-highlight";
import rehypeSanitize from 'rehype-sanitize'

const processor = await unified()
    // 解析为 md ast节点
    .use(remarkParse)
    // 处理 md 行尾结束符号
    .use(remarkBreaks)
    // 处理数学公式
    .use(remarkMath)
    // 转换gfm格式
    .use(remarkGfm)
    // 转换为html ast
    .use(remarkRehype)
    // 代码高亮
    // 渲染数学公式
    .use(rehypeMathjax)
    // 解析目录
    .use(slug)
    .use(toc)
    .use(rehypeMermaid, {
        strategy: 'inline-svg'
    })
    .use(rehypeSanitize)
    .use(rehypeHighlight)
    // 将ast转换为string
    .use(rehypeStringify);

const renderPanel = window.document.getElementById("render-id");

async function onDocumentChanged(doc) {
    const result = await processor.process(doc.toString());
    console.log("渲染结果: ", result);
    renderPanel.innerHTML = result;
}

async function onDocumentScroll(scroll) {
    console.log("文档滚动结果: ", scroll);
}

window.onload = () => {
    window.parent.addDocumentChangedListener(onDocumentChanged);
    window.parent.addDocumentScrollListener(onDocumentScroll)
}



