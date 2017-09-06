/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
const path = require("path");
const webpack = require("webpack");
const combineLoaders = require('webpack-combine-loaders');

module.exports = {
    context: path.resolve(__dirname, '..'),

    devtool: 'eval',

    entry: {
        'app': [
            'webpack-hot-middleware/client?path=http://localhost:3000/__webpack_hmr',
            './src/client/app.tsx'
        ]
    },

    output: {
        path: path.resolve(__dirname, '../resources/web/snd/'),
        publicPath: 'http://localhost:3000/',
        filename: '[name].js'
    },

    externals: {
        'react/addons': true,
        'react/lib/ExecutionEnvironment': true,
        'react/lib/ReactContext': true
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

    plugins: [
        new webpack.NoEmitOnErrorsPlugin(),
        new webpack.HotModuleReplacementPlugin()
    ],

    resolve: {
        extensions: [ '.jsx', '.js', '.tsx', '.ts' ]
    }
};
