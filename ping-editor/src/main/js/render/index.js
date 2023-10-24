import remarkParse from 'remark-parse'
import remarkRehype from 'remark-rehype'
import rehypeStringify from 'rehype-stringify'
import {unified} from 'unified'
import remarkGfm from "remark-gfm";
import remarkBreaks from "remark-breaks";
import remarkMath from "remark-math";
// import remarkToc from "remark-toc";
import './github.css';
// import rehypeKatex from "rehype-katex";
import rehypeMathjax from 'rehype-mathjax'
import {toc} from "@jsdevtools/rehype-toc";
import slug from "rehype-slug";
// import rehypeTwemojify from "rehype-twemojify";
import rehypeMermaid from 'rehype-mermaid';
import rehypeHighlight from "rehype-highlight";
import rehypeSanitize from 'rehype-sanitize'

function print(input) {
    console.log(input);
    return input;
}

const processor = await unified()
    // 解析为 md ast节点
    .use(remarkParse)
    // 创建目录
    // .use(remarkToc)
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
    // .use(rehypeKatex)
    // 解析目录
    .use(slug)
    .use(toc)
    // .use(rehypeTwemojify)
    .use(rehypeMermaid, {
        // The default strategy is 'inline-svg'
        // strategy: 'img-png'
        // strategy: 'img-svg'
        strategy: 'inline-svg'
        // strategy: 'pre-mermaid'
    })
    // .data('settings', {fragment: true})
    .use(rehypeSanitize)
    .use(rehypeHighlight)
    // 将ast转换为string
    .use(rehypeStringify);

const renderPanel = window.document.getElementById("render-id");

async function onChange(str) {
    const result = await processor.process(str.toString());
    console.log("渲染结果: ", result);
    renderPanel.innerHTML = result;
}


window.onload = () => {
    window.parent.addChannelListener(onChange);
}



