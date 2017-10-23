/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
require("babel-polyfill");
const webpack = require("webpack");
const path = require("path");
const combineLoaders = require('webpack-combine-loaders');

module.exports = {
    context: path.resolve(__dirname, '..'),

    devtool: 'source-map',

    entry: {
        'app': [
            'babel-polyfill',
            './src/client/app.tsx'
        ]
    },

    output: {
        path: path.resolve(__dirname, '../resources/web/snd/'),
        publicPath: './', // allows context path to resolve in both js/css
        filename: "[name].js"
    },

    module: {
        rules: [
            {
                test: /\.tsx?$/,
                loaders: ['babel-loader', 'ts-loader']
            },
            {
                test: /\.css$/,
                loader: combineLoaders([
                    {
                        loader: 'style-loader'
                    }, {
                        loader: 'css-loader',
                        query: {
                            modules: true,
                            localIdentName: '[name]__[local]___[hash:base64:5]'
                        }
                    }
                ])
            }
        ]
    },

    resolve: {
        extensions: [ '.jsx', '.js', '.tsx', '.ts' ]
    },

    plugins: [
        new webpack.DefinePlugin({
            'process.env.NODE_ENV': '"production"'
        })
    ]
};