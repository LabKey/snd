/*
 * Copyright (c) 2016-2017 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 */
const path = require("path");
var ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
    context: path.resolve(__dirname, '..'),

    entry: {
        'app': [
            './src/client/theme/style.js'
        ]
    },

    output: {
        path: path.resolve(__dirname, '../resources/web/snd'),
        publicPath: '/labkey/snd/',
        filename: 'style.js' // do not override app.js
    },

    module: {
        rules: [
            {
                test: /\.css$/,
                loader: ExtractTextPlugin.extract({
                    use: [{
                        loader: 'css-loader',
                        options: {
                            sourceMap: true
                        }
                    }],
                    fallback: 'style-loader'
                })
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

    plugins: [
        new ExtractTextPlugin({
            allChunks: true,
            filename: '[name].css'
        })
    ]
};