/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
const path = require("path");
const webpack = require("webpack");

module.exports = {
    context: path.resolve(__dirname, '..'),

    devtool: 'eval',

    entry: {
        'app': [
            'webpack-hot-middleware/client?path=http://localhost:3000/__webpack_hmr',
            './src/client/app.jsx'
        ]
    },

    output: {
        path: path.resolve(__dirname, '../resources/web/snd'),
        publicPath: 'http://localhost:3000/',
        filename: "[name].js"
    },

    module: {
        rules: [
            {
                test: /\.jsx?$/,
                loaders: ['babel-loader']
            }
        ]
    },

    resolve: {
        extensions: [ '.jsx', '.js', '.tsx', '.ts' ]
    },

    plugins: [
        new webpack.HotModuleReplacementPlugin(),
        new webpack.NoEmitOnErrorsPlugin()
    ]
};
