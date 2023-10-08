const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const process = require('process');
const {CleanWebpackPlugin} = require("clean-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const devMode = process.env.NODE_ENV === 'development' ? 'development' : 'production';

module.exports = {
    mode: devMode,
    entry: {
        main: path.join(__dirname, "./src/main/js/index.js"),
        edit: path.join(__dirname, "./src/main/js/edit/index.js"),
        render: path.join(__dirname, "./src/main/js/render/index.js"),
    },
    output: {
        filename: '[name]/[contenthash].js', // 使用 [contenthash] 占位符
        path: path.join(__dirname, './target/dist'),
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: [MiniCssExtractPlugin.loader, "css-loader", "postcss-loader"]
            },
        ]
    },
    devServer: {
        port: 8088,
        proxy: [
            {
                context: ['/v1'],
                target: 'http://localhost:10000',
            },
        ],
    },
    plugins: [
        new CleanWebpackPlugin({
            cleanOnceBeforeBuildPatterns: ['**/*'],
        }),
        // new webpack.HotModuleReplacementPlugin(),
        new MiniCssExtractPlugin({
            filename: '[name]/[contenthash].css',
        }),
        new HtmlWebpackPlugin({
            filename: 'index.html',
            template: './src/main/js/index.html',
            inject: true,
            hash: true,
            chunks: ["main"],
            minify: {
                removeComments: true,
                collapseWhitespace: true,
                removeAttributeQuotes: true
            }
        }),
        new HtmlWebpackPlugin({
            filename: 'edit/index.html',
            template: './src/main/js/edit/index.html',
            inject: true,
            hash: true,
            chunks: ["edit"],
            minify: {
                removeComments: true,
                collapseWhitespace: true,
                removeAttributeQuotes: true
            }
        }),
        new HtmlWebpackPlugin({
            filename: './render/index.html',
            template: './src/main/js/render/index.html',
            inject: true,
            hash: true,
            chunks: ["render"],
            minify: {
                removeComments: true,
                collapseWhitespace: true,
                removeAttributeQuotes: true
            }
        }),
    ],
    devtool: 'inline-source-map', // 设置开发模式下追踪代码 （展示报错的目标文件）
    optimization: {
        minimizer: [
            // 在 webpack@5 中，你可以使用 `...` 语法来扩展现有的 minimizer（即 `terser-webpack-plugin`），将下一行取消注释
            `...`,
        ],
    },

}