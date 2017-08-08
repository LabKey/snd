/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
const webpack = require("webpack");
const path = require("path");

module.exports = {
    context: path.resolve(__dirname, '..'),

    devtool: 'source-map',

    entry: {
        'app': [
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