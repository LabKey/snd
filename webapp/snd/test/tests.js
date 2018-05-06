

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
        },
        {
            name: 'Invalid Save Event: No JSON',
            run: function(){
                return{
                    request:{
                        url:LABKEY.SND_TEST_URLS.SAVE_EVENT_URL
                    },
                    expectedFailure:"Missing json parameter."
                }
            }
        },{
            name: 'Save Event: Missing Subject ID',
            run: function(){
                return{
                    request:{
                        url:LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
                        jsonData:{
                            eventId: 1800001,
                            date: "2018-02-26T17:51:20",
                            note: "This is a test event note.",
                            projectIdRev: '60|0',
                            qcState: 'Completed',
                            eventData: [
                                {
                                    superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                                    attributes: [{
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['propertyId'],
                                        value: '10'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['propertyId'],
                                        value: 'mL'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['propertyId'],
                                        value: 'red'
                                    }]
                                }
                            ]
                        }
                    },
                    expectedFailure:"Missing required json parameter: subjectId."
                }
            }

        },
        {
            name: 'Save Event: Missing Super Package ID in event data',
            run: function(){
                return{
                    request:{
                        url:LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
                        jsonData:{
                            eventId: 1800001,
                            subjectId: 1,
                            date: "2018-02-26T17:51:20",
                            note: "This is a test event note.",
                            projectIdRev: '60|0',
                            qcState: 'Completed',
                            eventData: [
                                {
                                    attributes: [{
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['propertyId'],
                                        value: '10'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['propertyId'],
                                        value: 'mL'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['propertyId'],
                                        value: 'red'
                                    }]
                                }
                            ]
                        }
                    },
                    expectedFailure:"Missing required json parameter: superPkgId for a top level package"
                }
            }
        },{
            name: 'Save Event: Missing attributes in event data',
            run: function(){
                return{
                    request:{
                        url:LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
                        jsonData:{
                            eventId: 1800001,
                            subjectId: 1,
                            date: "2018-02-26T17:51:20",
                            note: "This is a test event note.",
                            projectIdRev: '60|0',
                            qcState: 'Completed',
                            eventData: [
                                {
                                    superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                                    attribute: [{
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['propertyId'],
                                        value: '10'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['propertyId'],
                                        value: 'mL'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['propertyId'],
                                        value: 'red'
                                    }]
                                }
                            ]
                        }
                    },
                    expectedFailure:"Missing json parameter: attributes for a top level package"
                }
            }
            // TODO: Comment this in when we implement the required subpackages feature
        // }, {
        //     name: 'Save Event : Missing one of the package in subpackages',
        //     run : function() {
        //         return {
        //             request: {
        //                 url: LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
        //                 jsonData :{
        //                              eventId: 1800001,
        //                              subjectId: 2,
        //                              date: "2018-02-26T17:51:20",
        //                              note: "Note for event attribute data sample JSON",
        //                              projectIdRev: '61|0',
        //                              eventData: [
        //                          {
        //                              superPkgId: LABKEY.SND_PKG_CACHE['814']['superPkgId'],
        //                              attributes : [],
        //                              subPackages : [{
        //                                  superPkgId : LABKEY.getSubpackageSuperPkgId(810, LABKEY.SND_PKG_CACHE['814']['subPackages']),
        //                                  attributes: [
        //                                      {
        //                                          propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['810']['attributes'], 'amount')['propertyId'],
        //                                          value: 100
        //                                      }, {
        //                                          propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['810']['attributes'], 'units')['propertyId'],
        //                                          value: "mEq/L"
        //                                      }, {
        //                                          propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['810']['attributes'], 'kit_type')['propertyId'],
        //                                          value: "Sodium Colorimetric Detection Kit"
        //                                      }]
        //
        //                              }, {
        //                                  superPkgId: LABKEY.getSubpackageSuperPkgId(811, LABKEY.SND_PKG_CACHE['814']['subPackages']),
        //                                  attributes: [
        //                                      {
        //                                          propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['811']['attributes'], 'amount')['propertyId'],
        //                                          value: "200"
        //                                      }, {
        //                                          propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['811']['attributes'], 'units')['propertyId'],
        //                                          value: "mEq/L"
        //                                      }, {
        //                                          propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['811']['attributes'], 'kit_type')['propertyId'],
        //                                          value: "Potassium Detection Kit"
        //                                      }]
        //
        //                              },{
        //                                  superPkgId: LABKEY.getSubpackageSuperPkgId(813, LABKEY.SND_PKG_CACHE['814']['subPackages']),
        //                                  attributes: [
        //                                      {
        //                                          propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'amount')['propertyId'],
        //                                          value: "400"
        //                                      }, {
        //                                          propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'units')['propertyId'],
        //                                          value: "mEq/L"
        //                                      }, {
        //                                          propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'kit_type')['propertyId'],
        //                                          value: "Chloride Blood Detection Kit"
        //                                      }]
        //                              }]
        //                          }
        //                      ]
        //                  }
        //             },
        //             expectedFailure: "Missing data for subpackage 812 which contains required fields"
        //         }
        //     }

        }, {
            name: 'Save Event : Missing one of the attribute information in subpackages',
            run : function() {
                return {
                    request: {
                        url: LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
                        jsonData :{
                            eventId: 1800001,
                            subjectId: 2,
                            date: "2018-02-26T17:51:20",
                            note: "Note for event attribute data sample JSON",
                            projectIdRev: '61|0',
                            qcState: 'Completed',
                            eventData: [
                                {
                                    superPkgId: LABKEY.SND_PKG_CACHE['814']['superPkgId'],
                                    attributes : [],
                                    subPackages : [{
                                        superPkgId : LABKEY.getSubpackageSuperPkgId(810, LABKEY.SND_PKG_CACHE['814']['subPackages']),
                                        attributes: [
                                             {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['810']['attributes'], 'units')['propertyId'],
                                                value: "mEq/L"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['810']['attributes'], 'kit_type')['propertyId'],
                                                value: "Sodium Colorimetric Detection Kit"
                                            }]

                                    }, {
                                        superPkgId: LABKEY.getSubpackageSuperPkgId(811, LABKEY.SND_PKG_CACHE['814']['subPackages']),
                                        attributes: [
                                            {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['811']['attributes'], 'amount')['propertyId'],
                                                value: "200"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['811']['attributes'], 'units')['propertyId'],
                                                value: "mEq/L"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['811']['attributes'], 'kit_type')['propertyId'],
                                                value: "Potassium Detection Kit"
                                            }]

                                    },{
                                        superPkgId: LABKEY.getSubpackageSuperPkgId(812, LABKEY.SND_PKG_CACHE['814']['subPackages']),
                                        attributes: [
                                            {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['812']['attributes'], 'amount')['propertyId'],
                                                value: "300"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['812']['attributes'], 'units')['propertyId'],
                                                value: "mEq/L"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['812']['attributes'], 'kit_type')['propertyId'],
                                                value: "Carbon Dioxide (CO2) Colorimetric Detection Kit"
                                            }]

                                    },{
                                        superPkgId: LABKEY.getSubpackageSuperPkgId(813, LABKEY.SND_PKG_CACHE['814']['subPackages']),
                                        attributes: [
                                            {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'amount')['propertyId'],
                                                value: "400"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'units')['propertyId'],
                                                value: "mEq/L"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'kit_type')['propertyId'],
                                                value: "Chloride Blood Detection Kit"
                                            }]
                                    }]
                                }
                            ]
                        }
                    },
                    expectedFailure: "1 error found"
                }
            }

        },{
            name: 'Save Event : Missing one of the package information in subpackages',
            run : function() {
                return {
                    request: {
                        url: LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
                        jsonData :{
                            eventId: 1800001,
                            subjectId: 2,
                            date: "2018-02-26T17:51:20",
                            note: "Note for event attribute data sample JSON",
                            projectIdRev: '61|0',
                            qcState: 'Completed',
                            eventData: [
                                {
                                    superPkgId: LABKEY.SND_PKG_CACHE['814']['superPkgId'],
                                    attributes : [],
                                    subPackages : [
                                            {
                                        attributes: [
                                            {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['810']['attributes'], 'amount')['propertyId'],
                                                value: "100"
                                            },{
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['810']['attributes'], 'units')['propertyId'],
                                                value: "mEq/L"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['810']['attributes'], 'kit_type')['propertyId'],
                                                value: "Sodium Colorimetric Detection Kit"
                                            }]

                                    }, {
                                        superPkgId: LABKEY.getSubpackageSuperPkgId(811, LABKEY.SND_PKG_CACHE['814']['subPackages']),
                                        attributes: [
                                            {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['811']['attributes'], 'amount')['propertyId'],
                                                value: "200"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['811']['attributes'], 'units')['propertyId'],
                                                value: "mEq/L"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['811']['attributes'], 'kit_type')['propertyId'],
                                                value: "Potassium Detection Kit"
                                            }]

                                    },{
                                        superPkgId: LABKEY.getSubpackageSuperPkgId(812, LABKEY.SND_PKG_CACHE['814']['subPackages']),
                                        attributes: [
                                            {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['812']['attributes'], 'amount')['propertyId'],
                                                value: "300"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['812']['attributes'], 'units')['propertyId'],
                                                value: "mEq/L"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['812']['attributes'], 'kit_type')['propertyId'],
                                                value: "Carbon Dioxide (CO2) Colorimetric Detection Kit"
                                            }]

                                    },{
                                        superPkgId: LABKEY.getSubpackageSuperPkgId(813, LABKEY.SND_PKG_CACHE['814']['subPackages']),
                                        attributes: [
                                            {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'amount')['propertyId'],
                                                value: "400"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'units')['propertyId'],
                                                value: "mEq/L"
                                            }, {
                                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'kit_type')['propertyId'],
                                                value: "Chloride Blood Detection Kit"
                                            }]
                                    }]
                                }
                            ]
                        }
                    },
                    expectedFailure: "Missing required json parameter: superPkgId for a subPackage"
                }
            }

        },{
            name: 'Save Event: Multiple instance of event Data for same event',
            run: function(){
                return{
                    request:{
                        url:LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
                        jsonData:{
                            eventId: 1800002,
                            subjectId: 1,
                            date: "2018-02-26T17:51:20",
                            note: "This is a test event note.",
                            projectIdRev: '60|0',
                            qcState: 'Completed',
                            eventData: [
                                {
                                    superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                                    attributes: [{
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['propertyId'],
                                        value: '10'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['propertyId'],
                                        value: 'mL'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['propertyId'],
                                        value: 'red'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'regenDate')['propertyId'],
                                        value: '2018-03-14'
                                    }]
                                },{
                                    superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                                    attributes: [{
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['propertyId'],
                                        value: '20'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['propertyId'],
                                        value: 'mL'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['propertyId'],
                                        value: 'black'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'regenDate')['propertyId'],
                                        value: '2018-03-18'
                                    }]
                                }
                            ]
                        }
                    },
                    response: function(response){
                        if (response.status === 200) {
                            return true;
                        }

                        LABKEY.handleFailure(response, name + " - Stack Trace");
                        return false;
                    }
                }
            }

        }, {

            name: 'Save Event: Invalid eventID',
            run : function()
            {
                return{
                    request:{
                        url:LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
                        jsonData:{
                            eventId: "1800001-1",
                            subjectId : 1,
                            date: "2018-02-26T17:51:20",
                            note: "This is a test event note.",
                            projectIdRev: '60|0',
                            qcState: 'Completed',
                            eventData: [
                                {
                                    superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                                    attributes: [{
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['propertyId'],
                                        value: '10'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['propertyId'],
                                        value: 'mL'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['propertyId'],
                                        value: 'red'
                                    }]
                                }
                            ]
                        }
                    },
                    expectedFailure:"eventId is present but not a valid integer."
                }
            }

        },{
            name:'Save Event: Missing Project ID',
            run:function(){
                return{
                    request:{
                        url:LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
                        jsonData:{
                            eventId: 1800001,
                            subjectId:1,
                            date: "2018-02-26T17:51:20",
                            note: "Note for Save Event with Missing Project ID",
                            qcState: 'Completed',
                            eventData: [
                                {
                                    superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                                    attributes: [{
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['propertyId'],
                                        value: '10'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['propertyId'],
                                        value: 'mL'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['propertyId'],
                                        value: 'red'
                                    }]
                                }
                            ]

                        }
                    },
                    expectedFailure:"Missing required json parameter: projectIdRev."
                }
            }

        },{
            name:'Save Event: Missing Revision number for project Id',
            run:function(){
                return{
                    request:{
                        url:LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
                        jsonData:{
                            eventId : 1800002,
                            subjectId : 1,
                            date : "2018-03-03T17:21:22",
                            projectIdRev : "61",
                            note : "Note for Save Event with Missing Revision number for project Id",
                            qcState: 'Completed',
                            eventData: [
                                    {
                                        superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                                        attributes: [{
                                            propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['propertyId'],
                                            value: '10'
                                        }, {
                                            propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['propertyId'],
                                            value: 'mL'
                                        }, {
                                            propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['propertyId'],
                                            value: 'red'
                                        }]
                                    }
                            ]
                        }
                    },
                   expectedFailure :"Project Id|Rev not formatted correctly"
                }
            }

        },{
            name:'Save Event: Invalid project Id',
            run:function(){
                return{
                    request:{
                        url:LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
                        jsonData:{
                            eventId : 1800002,
                            subjectId : 1,
                            date : "2018-03-03T17:21:22",
                            projectIdRev : "64|0",
                            note : "Note for Save Event with Invalid project Id",
                            qcState: 'Completed',
                            eventData: [
                                {
                                    superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                                    attributes: [{
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['propertyId'],
                                        value: '10'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['propertyId'],
                                        value: 'mL'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['propertyId'],
                                        value: 'red'
                                    }]
                                }
                            ]
                        }
                    },
                    expectedFailure : "Project|revision not found: 64|0"
                }
            }

        },
        {
            name: 'Save Event: Invalid date',
            run: function () {
                return {
                    request: {
                        url: LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
                        jsonData: {
                            eventId: 1700007,
                            date: "xx",
                            subjectId: 1,
                            projectIdRev: "60|0",
                            note: "Note for Save Event with Invalid date",
                            qcState: 'Completed',
                            eventData: [
                                {
                                    superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                                    attributes: [{
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['propertyId'],
                                        value: '10'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['propertyId'],
                                        value: 'mL'
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['propertyId'],
                                        value: 'red'
                                    }]
                                }
                            ]
                        }
                    },
                    expectedFailure: "Unparseable date: \"xx\""
                }
            }

        },{

            name: 'Get Event: Invalid eventID - Alphanumeric character',
            run : function()
            {
                return{
                    request:{
                        url:LABKEY.SND_TEST_URLS.GET_EVENT_URL,
                        jsonData:{"eventId": "1800000a"}
                    },
                    expectedFailure : "eventId is present but not a valid integer."
                }
            }

        },{

            name: 'Get Event: Invalid eventID - Not present',
            run : function()
            {
                return{
                    request:{
                        url:LABKEY.SND_TEST_URLS.GET_EVENT_URL,
                        jsonData:{"eventId": "18000010"}
                    },
                    response:function(response,json)
                    {
                        if(response.status === 200)
                        {
                            return true;
                        }

                        LABKEY.handleFailure(response, name + " - Stack Trace");
                        return false;
                    }
                }
            }

        },{

            name: 'Get Event: Empty event',
            run : function()
            {
                return{
                    request:{
                        url:LABKEY.SND_TEST_URLS.GET_EVENT_URL,
                        jsonData:{"eventId": "18000010"}
                    },
                    response:function(response,json)
                    {
                        if(response.status === 200)
                        {
                            return true;
                        }

                        LABKEY.handleFailure(response, name + " - Stack Trace");
                        return false;
                    }
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
            var json;
            if (data && data.json) {
                json = data.json;
            } else if (data && data.event) {
                json = data.event;
            }
            if (json) {
                var match = matchExpected(expected, json);
                if (match === true) {
                    return true;
                }
                LABKEY.showMismatchData(test, "Actual: " + JSON.stringify(json));
                return 'Expected data does not match actual data for property "' + match + '"';
            }
            return 'Shape of response data is not expected.'
        }
        return false;
    }

    function sndAddEventTests () {
        var test, eventId, saveResponse, getResponse;
        LABKEY.getEventTestData().forEach(function(testData) {
            eventId = testData.jsonData['eventId'];
            test = {
                name: testData['name'] + ' - Save',
                roles: testData['roles']
            };

            // Set up save event request
            var saveRequest = {
                request: {
                    url: LABKEY.SND_TEST_URLS.SAVE_EVENT_URL,
                    jsonData: testData.jsonData
                }
            };
            
            if (testData['expectedFailure']) {
                saveResponse = {expectedFailure: testData['expectedFailure']};
                if (testData['expected']) {
                    saveResponse = Object.assign({}, saveResponse, {expected: testData['expected']})
                }
            } else {
                saveResponse = {
                    response: function (response) {
                        if (response && response.status === 200) {
                            return true;
                        }

                        LABKEY.handleFailure(response, test + " - Stack Trace");
                        return false;
                    }
                };
            }

            test.run = getRun(saveRequest, saveResponse);
            var getEventJson = {'eventId': eventId};
            if (testData.getEventParams) {
                getEventJson = Object.assign(getEventJson, testData.getEventParams)
            }

            // Set up get event request
            var getRequest = {
                request: {
                    url: LABKEY.SND_TEST_URLS.GET_EVENT_URL,
                    jsonData: getEventJson
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

    LABKEY.sndAddEventTests = sndAddEventTests;
    LABKEY.handleExpectedResponse = handleExpectedResponse;
    LABKEY.sndTestDriver = function() {
        new LABKEY.testDriver(TESTS);
    }

})(jQuery);
