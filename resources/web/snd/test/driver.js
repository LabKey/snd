

(function($) {

    var PROJECT,
            CONTAINER,
            tests = {},
            testOrder = [],
            beforeTestsFn,
            callbackCounter = 0;
            // SHOW_DATA = false;

    var TEST_URLS = {
        GET_EVENT_URL: LABKEY.ActionURL.buildURL('snd', 'getEvent.api'),
        SAVE_EVENT_URL: LABKEY.ActionURL.buildURL('snd', 'saveEvent.api'),
        SAVE_PKG_URL: LABKEY.ActionURL.buildURL('snd', 'savePackage.api'),
        GET_PKG_URL: LABKEY.ActionURL.buildURL('snd', 'getPackages.api'),
        SAVE_PROJECT_URL: LABKEY.ActionURL.buildURL('snd', 'saveProject.api')
    };

    var report = {
        completed: false,
        times: {},
        testsPassed: 0,
        testsRan: 0,
        testsTotal: 0,

        abort: function() {
            this.stopAll();
            this.completed = false;

            this.__display();
        },

        clear: function() {
            this.completed = false;
            this.times = {};
            this.testsPassed = 0;
            this.testsRan = 0;
            this.testsTotal = 0;
        },

        complete: function() {
            this.stopAll();
            this.completed = true;

            this.__display();
        },

        summary: function() {
            if (this.completed) {
                var total = this.times['~~total~~'];
                var totalTime = total.stop - total.start;
                return 'Complete in ' + totalTime + ' ms. Passed tests: ' + this.testsPassed + ', Failed tests: ' + (this.testsRan - this.testsPassed) + ', Total tests: ' + this.testsTotal;
            }

            return 'Aborted.';
        },

        start: function(name) {
            if (!this.times['~~total~~']) {
                this.times['~~total~~'] = {
                    start: new Date()
                }
            }

            if (!this.times[name]) {
                this.times[name] = {
                    start: new Date()
                };
            }
            else {
                console.warn('Attempted to start timer "' + '" multiple times.');
            }
        },

        stop: function(name) {
            if (this.times[name]) {
                this.times[name].stop = new Date();
            }
            else {
                console.warn('Attempted to stop timer "' + name + '" before it was started');
            }
        },

        stopAll: function() {
            for (var t in this.times) {
                if (this.times.hasOwnProperty(t)) {
                    if (!this.times[t].stop) {
                        this.stop(t);
                    }
                }
            }
        },

        __display: function() {
            console.log('completed:', this.completed);
            console.log('times (in ms):\n');
            for (var t in this.times) {
                if (t !== '~~total~~' && this.times.hasOwnProperty(t)) {
                    console.log('\t' + t + ':', this.times[t].stop - this.times[t].start);
                }
            }
            console.log('tests passed:', this.testsPassed);
            console.log('tests ran:', this.testsRan);
            console.log('tests total:', this.testsTotal);
        }
    };

    function log(msg, status, clear, append) {
        var log = $('.snd-test-log');

        if (clear) {
            log.html(msg);
        }
        else {
            log.html(log.html() + (append ? '' : '<br>') + msg);
        }

        var logDiv = $('.test-log');
        if (status === 'success') {
            logDiv.addClass('test-log-success');
            logDiv.removeClass('test-log-failure');
        }
        else if (status === 'failure') {
            logDiv.addClass('test-log-failure');
            logDiv.removeClass('test-log-success');
        }
        else {
            logDiv.removeClass('test-log-success test-log-failure');
        }
        logDiv.scrollTop(logDiv.prop('scrollHeight'));
    }

    function handleFailure(json, msg) {
        var jsonResponse = null;
        if (json.exception) {
            jsonResponse = json;
        }
        else if (json.responseText) {
            jsonResponse = JSON.parse(json.responseText);
        }
        if (jsonResponse && jsonResponse.exception) {
            msg += (': ' + jsonResponse.exception);
        }

        log(msg, 'failure', true);

        if (jsonResponse && jsonResponse.stackTrace) {
            showStackTrace(jsonResponse.exception, jsonResponse.stackTrace)
        }
    }

    function renderTests() {
        var html = ['<table>'];

        html.push(
                '<thead>',
                '<tr>',
                '<th class="test-results-list"><b>Test Name</b></th>',
                '<th class="test-results-list"><b>Status</b></th>',
                '<th><b>Reason</b></th>',
                '</tr>',
                '</thead>'
        );

        html.push('<tbody>');

        for (var t in tests) {
            if (tests.hasOwnProperty(t)) {
                var status = tests[t].status;
                var statusColor = tests[t].error ? '#ED2C10' : (status === 'passed' ? '#A5DDAD' : 'black');

                var indent = '';
                for (var i = 0; i < tests[t].depth; i++) {
                    indent += '&nbsp;&nbsp;';
                }

                html.push(
                        '<tr>',
                        '<td class="test-results-list">' + indent + tests[t].name + '</td>',
                        '<td class="test-results-list"><span style="color: ' + statusColor + '">' + tests[t].status + '</span></td>',
                        '<td>' + (tests[t].error ? tests[t].error : '') + '</td>',
                        '</tr>'
                );
            }
        }

        html.push('</tbody>');
        html.push('</table>');

        $('.snd-test-runner-frame').html(html.join(''));
    }

    function registerTest(test, parent, depth) {

        if (!test.name) {
            throw new Error('Each test requires a name');
        }
        if (tests[test.name]) {
            throw new Error('Test with name "' + test.name + '" was previously registered.');
        }

        test.error = undefined;
        test.status = 'not run';
        test.depth = depth;
        tests[test.name] = test;
        testOrder.push(test.name);

        test.parent = undefined;
        if (parent) {
            test.parent = parent.name;
        }

        renderTests();
    }

    function initTests(tests, parent, depth) {
        for (var t = 0; t < tests.length; t++) {
            if (tests[t].disabled !== true) {
                registerTest(tests[t], parent, depth);

                if (tests[t].dependents) {
                    initTests(tests[t].dependents, tests[t], depth+1);
                }
            }
        }
    }

    function showStackTrace(exception, stackTrace) {
        var html = ['<div>'];
        html.push(exception + '<br>');

        for (var s = 0; s < stackTrace.length; s++) {
            if (s > 10) {
                html.push('...');
                break;
            }
            html.push(stackTrace[s] + '<br>');
        }

        html.push('</div>');
        $('.snd-test-runner-frame').html(html.join(''));
    }

    // Save packages one at a time
    function savePackage(index, cb) {
        var pkgIds = [];
        var packageData = LABKEY.getInitData().BEFORE_ALL_TESTS.INIT_PACKAGES[index].jsonData;

        for (var i = 0; i < packageData.subPackages.length; i++) {
            pkgIds.push(packageData.subPackages[i].pkgId)
        }

        LABKEY.Query.selectRows({
            schemaName: 'snd',
            queryName: 'TopLevelSuperPkgs',
            columns: ['SuperPkgId', 'PkgId'],
            scope: this,
            filterArray: [LABKEY.Filter.create('PkgId', pkgIds.join(';'), LABKEY.Filter.Types.IN)],
            failure: function (json) {
                handleFailure(json, 'Failed package initialization');
            },
            success: function(results) {
                var subPackages = [];

                for (var r = 0; r < results.rows.length; r++) {
                    for (var p = 0; p < packageData.subPackages.length; p++) {
                        if (packageData.subPackages[p].pkgId === results.rows[r]["PkgId"]) {
                            packageData.subPackages[p].superPkgId = results.rows[r]["SuperPkgId"];
                            subPackages.push(packageData.subPackages[p]);
                        }
                    }
                }

                packageData.subPackages = subPackages;

                LABKEY.Ajax.request({
                    url: TEST_URLS.SAVE_PKG_URL,
                    jsonData: packageData,
                    scope: this,
                    failure: function (json) {
                        handleFailure(json, 'Failed package initialization');
                    },
                    success: function() {
                        // Only call callback when all have returned
                        index++;
                        if (index === LABKEY.getInitData().BEFORE_ALL_TESTS.INIT_PACKAGES.length) {
                            cb();
                        } else {
                            var msg = 'Initializing data...';
                            for (var d = 0; d < index; d++) {
                                msg += '.'
                            }

                            log(msg, null, true);

                            savePackage(index, cb);
                        }
                    }
                });
            }
        });
    }

    // Save projects in parallel
    function saveProject(projectData, cb) {
        var pkgIds = [];

        for (var i = 0; i < projectData.projectItems.length; i++) {
            pkgIds.push(projectData.projectItems[i].pkgId)
        }

        LABKEY.Query.selectRows({
            schemaName: 'snd',
            queryName: 'TopLevelSuperPkgs',
            columns: ['SuperPkgId', 'PkgId'],
            scope: this,
            filterArray: [LABKEY.Filter.create('PkgId', pkgIds.join(';'), LABKEY.Filter.Types.IN)],
            failure: function (json) {
                handleFailure(json, 'Failed project initialization');
            },
            success: function(results) {
                var projectItems = [];
                for (var r = 0; r < results.rows.length; r++) {
                    for (var p = 0; p < projectData.projectItems.length; p++) {
                        if (projectData.projectItems[p].pkgId === results.rows[r]["PkgId"]) {
                            projectData.projectItems[p].superPkgId = results.rows[r]["SuperPkgId"];
                            projectItems.push(projectData.projectItems[p]);
                        }
                    }
                }
                projectData.projectItems = projectItems;

                LABKEY.Ajax.request({
                    url: TEST_URLS.SAVE_PROJECT_URL,
                    jsonData: projectData,
                    scope: this,
                    failure: function (json) {
                        handleFailure(json, 'Failed project initialization');
                    },
                    success: function() {
                        // Only call callback when all have returned
                        callbackCounter++;
                        if (callbackCounter === LABKEY.getInitData().BEFORE_ALL_TESTS.INIT_PROJECTS.length ) {
                            cb();
                        }
                    }
                });
            }
        });
    }

    function initPackageData(cb) {
        log('Initializing data...', null, true);

        savePackage(0, cb);
    }

    function initProjectData(cb) {
        callbackCounter = 0;

        for (var i = 0; i < LABKEY.getInitData().BEFORE_ALL_TESTS.INIT_PROJECTS.length; i++) {
            saveProject(LABKEY.getInitData().BEFORE_ALL_TESTS.INIT_PROJECTS[i].jsonData, cb);
        }
    }

    function initData(cb) {
        initPackageData(function initProjects() {initProjectData(cb);});
    }

    function deleteProject(projectId, cb) {
        LABKEY.Query.selectRows({
            schemaName: 'snd',
            queryName: 'Projects',
            columns: ['ObjectId'],
            scope: this,
            filterArray: [LABKEY.Filter.create('ProjectId', projectId, LABKEY.Filter.Types.EQUALS)],
            failure: function (json) {
                handleFailure(json, 'Failed project delete');
            },
            success: function(results) {
                if (results.rows.length > 0) {
                    var objectId = results.rows[0]["ObjectId"].value;

                    LABKEY.Query.deleteRows({
                        schemaName: 'snd',
                        queryName: 'Projects',
                        rows: [{'ObjectId': objectId}],
                        scope: this,
                        failure: function (json) {
                            handleFailure(json, 'Failed project delete');
                        },
                        success: function () {
                            // Only call callback when all have returned
                            callbackCounter++;
                            if (callbackCounter === LABKEY.getInitData().BEFORE_ALL_TESTS.INIT_PROJECTS.length) {
                                cb();
                            }
                        }
                    });
                }
                else if (++callbackCounter === LABKEY.getInitData().BEFORE_ALL_TESTS.INIT_PROJECTS.length) {
                    cb();
                }
            }
        });
    }

    function deletePackage(pkgId, cb) {

        LABKEY.Query.deleteRows({
            schemaName: 'snd',
            queryName: 'Pkgs',
            rows: [{'PkgId': pkgId}],
            scope: this,
            failure: function (json) {
                handleFailure(json, 'Failed project delete');
            },
            success: function () {
                // Only call callback when all have returned
                callbackCounter++;
                if (callbackCounter === LABKEY.getInitData().BEFORE_ALL_TESTS.INIT_PACKAGES.length) {
                    cb();
                }
            }
        });
    }

    function cleanProjects(cb) {
        callbackCounter = 0;

        for (var i = 0; i < LABKEY.getInitData().BEFORE_ALL_TESTS.INIT_PROJECTS.length; i++) {
            deleteProject(LABKEY.getInitData().BEFORE_ALL_TESTS.INIT_PROJECTS[i].jsonData.projectId, cb);
        }
    }

    function cleanPackages(cb) {
        callbackCounter = 0;

        for (var i = 0; i < LABKEY.getInitData().BEFORE_ALL_TESTS.INIT_PACKAGES.length; i++) {
            deletePackage(LABKEY.getInitData().BEFORE_ALL_TESTS.INIT_PACKAGES[i].jsonData.id, cb);
        }
    }

    function cleanData() {
        cleanProjects(
                function() { cleanPackages(
                        function() {
                            log("Data cleaned", 'success', true);
                            log('Tests are ready to run.', undefined, false); }
                );});
    }

    function deleteEvents(cb) {
        for (var i = 0; i < LABKEY.getCleanData().EVENTIDS.length; i++) {
            LABKEY.Query.deleteRows({
                schemaName: 'snd',
                queryName: 'Events',
                rows: [{'EventId': LABKEY.getCleanData().EVENTIDS[i]}],
                scope: this,
                failure: function (json) {
                    handleFailure(json, 'Failed event delete');
                },
                success: function () {
                    // Only call callback when all have returned
                    callbackCounter++;
                    if (callbackCounter === LABKEY.getCleanData().EVENTIDS.length) {
                        log('Test data cleaned.', null, true);
                        log('Tests are ready to run.', null, false);
                        if (cb && cb instanceof Function) {
                            cb();
                        }
                    }
                }
            });
        }
    }

    function cleanTestData(cb) {
        callbackCounter = 0;

        deleteEvents(cb);
    }

    function cachePkgs(cb) {
        LABKEY.SND_PKG_CACHE = {};

        LABKEY.Query.selectRows({
            schemaName: 'snd',
            queryName: 'Pkgs',
            columns: ['PkgId'],
            scope: this,
            failure: function (json) {
                handleFailure(json, 'Failed package caching');
            },
            success: function (results) {
                var pkgIds = [];
                for (var i = 0; i < results.rows.length; i++) {
                    pkgIds.push(results.rows[i]['PkgId']);
                }

                LABKEY.Ajax.request({
                    url: TEST_URLS.GET_PKG_URL,
                    jsonData: {packages: pkgIds},
                    scope: this,
                    failure: function (json) {
                        handleFailure(json, 'Failed package caching');
                    },
                    success: function (json) {
                        var responseJson = JSON.parse(json.response).json;
                        for (var j = 0; j < responseJson.length; j++) {
                            LABKEY.SND_PKG_CACHE[responseJson[j].pkgId] = responseJson[j];
                        }
                        LABKEY.SND_PKG_CACHE['loaded'] = true;

                        cb();
                    }
                });
            }
        });
    }

    function init(tests, beforeTests) {

        // initDomains(domains);
        initTests(tests, null, 0);

        if ($.isFunction(beforeTests)) {
            beforeTestsFn = beforeTests;
        }

        renderTests();
        log('Tests are ready to run.', undefined, true);
    }

    function reset() {

        // reset report
        report.clear();
        report.testsTotal = testOrder.length;

        // reset tests
        for (var t in tests) {
            if (tests.hasOwnProperty(t)) {
                tests[t].error = undefined;
                tests[t].status = 'not run';
            }
        }

        // renderData();
    }

    function runAllCleanTests() {

        reset();

        report.start('setup');

        CONTAINER = LABKEY.container.name;

        log('Running tests...', undefined, true);

        if ($.isFunction(beforeTestsFn)) {
            beforeTestsFn();
        }

        report.stop('setup');

        report.start('testing');
        testRunner(-1, function () {
            report.stop('testing');
            report.complete();
            var status = 'failure';
            if (report.completed && (report.testsRan - report.testsPassed) === 0) {
                status = 'success';
            }
            log(report.summary(), status);
        });
    }

    function runAllTests() {

        cleanTestData(runAllCleanTests);
    }

    function runTest(test, context, cb) {
        report.start('test - ' + test.name);

        var finish = function(error, context) {
            report.stop('test - ' + test.name);
            var passed = error === undefined;

            if (passed) {
                report.testsPassed++;
            }
            report.testsRan++;

            cb({
                context: context,
                passed: passed,
                error: error
            });
        };

        var compareResponse = function(actualJson, expectedObject) {
            var actualResponse = JSON.stringify(actualJson);
            var expectedResponse = JSON.stringify(expectedObject);

            if (actualResponse === expectedResponse) {
                finish();
            }
            else {
                finish('Test "' + test.name + '" response did not match expected response.<br/>Expected: "' + expectedResponse + '"<br/>Actual: "' + actualResponse + '"');
            }
        };

        if (test.run === undefined) {
            finish('Test "' + test.name + '" is not runnable. Configured improperly, expected run() to be implemented');
        }
        else {
            var testRun;

            try {
                testRun = test.run(context);
            }
            catch (e) {
                var msg;
                if (LABKEY.Utils.isString(e)) {
                    msg = e;
                }
                else {
                    msg = e.message;
                    console.error('Stacktrace for test \"' + test.name + '\"');
                    console.error(e.stack);
                }

                finish('Test "' + test.name + '" failed. An uncaught error was thrown: \"' + msg + '\". See console for additional output.');
                return;
            }

            if (LABKEY.Utils.isString(testRun)) {
                finish('Test "' + test.name + '" failed. ' + testRun);
            }
            else if (testRun !== undefined && testRun.request !== undefined && (testRun.response !== undefined || testRun.expectedFailure)) {

                var testRequest = {
                    url: testRun.request.url,
                    method: testRun.request.method || null,
                    jsonData: $.isFunction(testRun.request.jsonData) ? testRun.request.jsonData() : testRun.request.jsonData
                };

                if (testRun.request.headers) {
                    testRequest.headers = testRun.request.headers;
                }

                if (testRun.expectedFailure) {
                    testRequest.success = function() {
                        finish('Test "' + test.name + '" unexpectedly succeeded.');
                    };
                    testRequest.failure = function(error) {

                        var json;

                        try {
                            json = JSON.parse(error.responseText);
                        }
                        catch (e) {
                            // could have responded html...
                            finish('Test "' + test.name + '" unexpectedly failed. Response not JSON.');
                            return;
                        }

                        if ($.isFunction(testRun.expectedFailure)) {
                            var result = testRun.expectedFailure(error, json);
                            if (result === true) {
                                finish();
                            }
                            else if (LABKEY.Utils.isString(result)) {
                                finish('Test "' + test.name + '" failed. ' + result);
                            }
                            else {
                                finish('Test "' + test.name + '" failed. Test should specify a reason for failure or return true.');
                            }
                        }
                        else {
                            if (json.exception === testRun.expectedFailure) {
                                finish();
                            }
                            else {
                                finish('Test "' + test.name + '" failed.<br>Expected: \"' + testRun.expectedFailure + '\"<br>Actual: \"' + json.exception + '\"');
                            }
                        }
                    }
                }
                else {
                    testRequest.success = function(response) {
                        var json;

                        try {
                            json = JSON.parse(response.responseText);
                        }
                        catch (e) {
                            // could have responded html...
                            finish('Test "' + test.name + '" unexpectedly failed. Response not JSON.');
                            return;
                        }

                        if ($.isFunction(testRun.response)) {
                            var result = testRun.response(response, json);
                            if (result === true) {
                                finish();
                            }
                            else if (LABKEY.Utils.isString(result)) {
                                finish('Test "' + test.name + '" failed. ' + result);
                            }
                            else if (LABKEY.Utils.isObject(result)) {
                                finish(undefined, result);
                            }
                            else {
                                finish('Test "' + test.name + '" failed. Test should specify a reason for failure or return true.');
                            }
                        }
                        else {
                            compareResponse(json, testRun.response);
                            return;
                        }
                    };
                    testRequest.failure = function(error) {
                        if (error) {
                            try {
                                var json = JSON.parse(error.responseText);

                                if (LABKEY.Utils.isObject(testRun.response)) {
                                    compareResponse(json, testRun.response);
                                    return;
                                }
                                else if (json && json.exception) {
                                    finish(error.status + ': ' + json.exception);
                                }
                            }
                            catch (e) {
                                // could have responded html...
                                finish('Test "' + test.name + '" unexpectedly failed. Response not JSON.');
                            }
                        }
                        else {
                            finish('Test "' + test.name + '" unexpectedly failed. No error provided.');
                        }
                    };
                }

                LABKEY.Ajax.request(testRequest);
            }
            else {
                finish('Test "' + test.name + '" is not runnable. Returned improper configuration.');
            }
        }
    }

    function testRunner(index, cb) {

        if (testOrder.length === 0) {
            renderTests();
            cb();
        }
        else if (index < testOrder.length - 1) {

            index++;
            var context;
            var test = tests[testOrder[index]];

            if (test.parent) {
                var parent = tests[test.parent];
                if (parent.error !== undefined) {
                    test.error = 'Not run due to failure of \"' + test.parent + '\"';
                    testRunner(index, cb);
                    return;
                }

                context = parent.context;
            }

            test.status = 'Running...';
            renderTests();

            runTest(test, context, function(result) {
                test.status = result.error ? 'failed' : 'passed';
                if (result.error) {
                    test.error = result.error;
                }

                if (result.context) {
                    test.context = result.context;
                }

                testRunner(index, cb);
            });
        }
        else {
            renderTests();
            cb();
        }
    }

    function getRunBtn() {
        return $('.snd-test-run-btn');
    }

    function getCleanInitBtn() {
        return $('.snd-test-clean-init-btn');
    }

    function getCleanTestBtn() {
        return $('.snd-test-clean-test-btn');
    }

    $(function() {
        // bind inputs
        getRunBtn().on('click', runAllTests);
        getCleanInitBtn().on('click', cleanData);
        getCleanTestBtn().on('click', cleanTestData);
    });

    LABKEY.testDriver = function(tests, beforeTests) {
        $(function() {
            init(tests, beforeTests);
        });
    };

    LABKEY.getAttributeByName = function (attributes, name) {
        for (var i = 0; i < attributes.length; i++) {
            if (attributes[i].name === name) {
                return attributes[i];
            }
        }
        return null;
    };

    LABKEY.initData = function (cb) {
        initData(
            function () {
                cachePkgs(cb);
            }
        )
    };


    LABKEY.SND_TEST_URLS = TEST_URLS;

})(jQuery);