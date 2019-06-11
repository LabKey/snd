/*
 * Copyright (c) 2017-2018 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
require("babel-polyfill");
const webpack = require("webpack");
const path = require("path");
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

module.exports = {
    context: path.resolve(__dirname, '..'),

    mode: 'production',

    devtool: 'source-map',

    entry: {
        'app': [
            './src/client/theme/style.js',
            './src/client/app.tsx'
        ]
    },

    output: {
        path: path.resolve(__dirname, '../resources/web/snd/app/'),
        publicPath: './', // allows context path to resolve in both js/css
        filename: "[name].js"
    },

    module: {
        rules: [
            {
                test: /\.tsx?$/,
                loaders: [{
                    loader: 'babel-loader',
                    options: {
                        babelrc: true,
                        cacheDirectory: true,
                        presets: [
                            "@babel/preset-env",
                            "@babel/preset-react"
                        ]
                    }
                },{
                    loader: 'ts-loader',
                    options: {
                        onlyCompileBundledFiles: true
                        // this flag and the test regex will make sure that test files do not get bundled
                        // see: https://github.com/TypeStrong/ts-loader/issues/267
                    }
                }]
            },
            {
                test: /\.css$/,
                use: [
                    MiniCssExtractPlugin.loader,
                    'css-loader'
                ]
            },
            {
                test: /style.js/,
                loaders: [{
                    loader: 'babel-loader',
                    options: {
                        cacheDirectory: true
                    }
                }]
            }
        ]
    },

    resolve: {
        extensions: [ '.jsx', '.js', '.tsx', '.ts' ]
    },

    plugins: [
        new webpack.DefinePlugin({
            'process.env.NODE_ENV': '"production"'
        }),
        new MiniCssExtractPlugin({
            filename: '[name].css'
        }),
    ]
};