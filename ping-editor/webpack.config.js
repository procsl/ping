const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const process = require('process');
const {CleanWebpackPlugin} = require("clean-webpack-plugin");
const TerserPlugin = require('terser-webpack-plugin');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
module.exports = {
    mode: process.env.NODE_ENV === 'development' ? 'development' : 'production',
    entry: path.join(__dirname, "./src/main/js/index.js"),
    output: {
        filename: '[name].[contenthash].js', // 使用 [contenthash] 占位符
        path: path.join(__dirname, './target/dist'),
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: [MiniCssExtractPlugin.loader, "css-loader", "postcss-loader"]
            }
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
        new HtmlWebpackPlugin({
            filename: 'index.html',
            template: './src/main/js/index.html',
            inject: true,
            minify: {
                removeComments: true,
                collapseWhitespace: true,
                removeAttributeQuotes: true
            }
        }),
        new CleanWebpackPlugin({
            cleanOnceBeforeBuildPatterns: ['**/*'],
        }),
        new MiniCssExtractPlugin({
            filename: 'main.css'
        })
    ],
    devtool: 'inline-source-map', // 设置开发模式下追踪代码 （展示报错的目标文件）
    optimization: {
        minimize: true, // 启用优化
        minimizer: [
            new TerserPlugin({
                // 配置选项
                terserOptions: {
                    // 例如，启用混淆
                    mangle: true,
                    // ...其他 terser 选项...
                },
            }),
        ],
        splitChunks: {
            cacheGroups: {
                chunks: 'async',
                vendor: {
                    test: /[\\/]node_modules[\\/]/,
                    name: 'vendors',
                    chunks: 'all',
                },
            },
        },
    },

}