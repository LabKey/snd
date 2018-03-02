

(function($) {

    var TESTS = [
        {
            name: 'Invalid Get Event: No JSON',
            run: function () {
                return {
                    request: {
                        url: LABKEY.SND_TEST_URLS.GET_EVENT_URL
                    },
                    expectedFailure: "Missing json parameter."
                }
            }
        }
    ];



    function matchExpected(expected, actual) {
        return LABKEY.equals(actual, deepmerge(actual, expected));
    }

    function getRun(request, response) {
        return function() {
            return Object.assign({}, request, response);
        }
    }

    function handleExpectedResponse(test, expected, response, data) {
        if (response && response.status === 200) {
            if (data && data.json) {
                if (matchExpected(expected, data.json)) {
                    return true;
                }
                LABKEY.showMismatchData(test, "Actual: " + JSON.stringify(data.json));
                return 'Expected data does not match actual data'
            }
            return 'Shape of response data is not expected.'
        }
        return false;
    }

    function addSndEventTests () {
        var test, testData, eventId, saveResponse, getResponse;
        LABKEY.getEventTests().forEach(function(testData) {
            eventId = testData.jsonData['eventId'];
            test = {name: testData['name'] + ' - Save'};

            // Set up save event request
            var saveRequest = {
                request: {
                    url: LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
                    jsonData: testData.jsonData
                }
            };
            
            if (testData['expectedFailure']) {
                saveResponse = {expectedFailure: testData['expectedFailure']};
            } else {
                saveResponse = {
                    response: function (response) {
                        if (response && response.status === 200) {
                            return true;
                        }

                        return false;
                    }
                };
            }

            test.run = getRun(saveRequest, saveResponse);

            // Set up get event request
            var getRequest = {
                request: {
                    url: LABKEY.SND_TEST_URLS.GET_EVENT_URL,
                    jsonData: {'eventId': eventId}
                }
            };

            if (testData['expected']) {
                getResponse = {
                    response: function(response, data) {
                        return handleExpectedResponse(testData['name'], testData['expected'], response, data)
                    }
                };
            }
            else {
                getRequest = {
                    response: function (response) {
                        if (response && response.status === 200) {
                            return true;
                        }

                        return false;
                    }
                }
            }

            if (!testData['expectedFailure']) {
                test.dependents = [
                    {
                        name: testData['name'] + ' - Get',
                        run: getRun(getRequest, getResponse)
                    }
                ];
            }

            TESTS.push(test);
        }, this);
    }

    LABKEY.initData(function() {
        addSndEventTests();
        new LABKEY.testDriver(TESTS);
    });

})(jQuery);
