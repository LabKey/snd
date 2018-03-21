
(function($) {

    var SNDTEST_INIT_DATA = {
        BEFORE_ALL_TESTS: {
            INIT_CATEGORIES: [
                {
                    CategoryId: 20,
                    Description:  'ChlorideTestTrigger',
                    Active: true,
                    Comment: 'This is chloride test trigger validation.'
                },
                {
                    CategoryId: 21,
                    Description:  'ChlorideBloodTestTrigger',
                    Active: true,
                    Comment: 'This is chloride blood test trigger validation.'
                },
                {
                    CategoryId: 22,
                    Description:  'ElectrolytesTestTrigger',
                    Active: true,
                    Comment: 'This is electrolytes test trigger validation.'
                }
            ],
            INIT_PACKAGES: [
                {
                    jsonData: {
                        id: 810,
                        description: 'Sodium blood test',
                        active: true,
                        repeatable: true,
                        narrative: 'Sodium: {amount} {units} measured using {kit_type}',
                        categories: [],
                        subPackages: [],
                        attributes: [
                            {
                                name: 'amount',
                                label: 'Amount',
                                rangeURI: 'int',
                                required: true,
                                validators:[{
                                    expression:"~gte=1&~lte=500",
                                    name:"SND Range",
                                    description:"SND Numeric Range",
                                    type:"range"}]
                            }, {
                                name: 'units',
                                label: 'Units',
                                rangeURI: 'string',
                                required: true
                            }, {
                                name: 'kit_type',
                                label: 'Kit',
                                rangeURI: 'string',
                                required: true
                            }
                        ]
                    }
                }, {
                    jsonData: {
                        id: 811,
                        description: 'Potassium blood test',
                        active: true,
                        repeatable: true,
                        narrative: 'Potassium: {amount} {units} measured using {kit_type}',
                        categories: [],
                        subPackages: [],
                        attributes: [
                            {
                                name: 'amount',
                                label: 'Amount',
                                rangeURI: 'int',
                                required: true
                            }, {
                                name: 'units',
                                label: 'Units',
                                rangeURI: 'string',
                                required: true

                            }, {
                                name: 'kit_type',
                                label: 'Kit',
                                rangeURI: 'string',
                                required: true
                            }
                        ]
                    }
                }, {
                    jsonData: {
                        id: 812,
                        description: 'Carbon Dioxide, Bicarbonate (CO2) Test',
                        active: true,
                        repeatable: true,
                        narrative: 'Bicarbonate: {amount} {units} measured using {kit_type}',
                        categories: [],
                        subPackages: [],
                        attributes: [
                            {
                                name: 'amount',
                                label: 'Amount',
                                rangeURI: 'int',
                                required: true
                            }, {
                                name: 'units',
                                label: 'Units',
                                rangeURI: 'string',
                                required: true

                            }, {
                                name: 'kit_type',
                                label: 'Kit',
                                rangeURI: 'string',
                                required: true
                            }
                        ]
                    }
                }, {
                    jsonData: {
                        id: 813,
                        description: 'Chloride blood test',
                        active: true,
                        repeatable: true,
                        narrative: 'Chloride: {amount} {units} measured using {kit_type}',
                        categories: [20, 21],
                        subPackages: [],
                        attributes: [
                            {
                                name: 'amount',
                                label: 'Amount',
                                rangeURI: 'int',
                                required: true
                            }, {
                                name: 'units',
                                label: 'Units',
                                rangeURI: 'string',
                                required: true

                            }, {
                                name: 'kit_type',
                                label: 'Kit',
                                rangeURI: 'string',
                                required: true
                            }
                        ]
                    }
                }, {
                    jsonData: {
                        id: 814,
                        description: 'Elecrolyte Tests',
                        active: true,
                        repeatable: true,
                        narrative: 'Electrolytes',
                        categories: [22],
                        subPackages: [{
                            pkgId: 810,
                            sortOrder: 2
                        }, {
                            pkgId: 811,
                            sortOrder: 1
                        }, {
                            pkgId: 812,
                            sortOrder: 3
                        }, {
                            pkgId: 813,
                            sortOrder: 4
                        }],
                        attributes: []
                    }
                }, {
                    jsonData: {
                        id: 815,
                        description: 'Blood Urea Nitrogen (BUN)',
                        active: true,
                        repeatable: true,
                        narrative: 'BUN: {concentration} {units} measured using {kit_type} test',
                        categories: [],
                        subPackages: [],
                        attributes: [
                            {
                                name: 'concentration',
                                label: 'Concentration',
                                rangeURI: 'int',
                                required: true
                            }, {
                                name: 'units',
                                label: 'Units',
                                rangeURI: 'string',
                                required: true

                            }, {
                                name: 'kit_type',
                                label: 'Kit',
                                rangeURI: 'string',
                                required: true
                            }
                        ]
                    }
                }, {
                    jsonData: {
                        id: 816,
                        description: 'Calcium blood test',
                        active: true,
                        repeatable: true,
                        narrative: 'Calcium: {amount} {units} measured using {kit_type} test',
                        categories: [],
                        subPackages: [],
                        attributes: [
                            {
                                name: 'amount',
                                label: 'Amount',
                                rangeURI: 'int',
                                required: true
                            }, {
                                name: 'units',
                                label: 'Units',
                                rangeURI: 'string',
                                required: true

                            }, {
                                name: 'kit_type',
                                label: 'Kit',
                                rangeURI: 'string',
                                required: true
                            }
                        ]
                    }
                }, {
                    jsonData: {
                        id: 817,
                        description: 'Glucose blood test',
                        active: true,
                        repeatable: true,
                        narrative: 'Glucose: {amount} {units} measured using {kit_type} test',
                        categories: [],
                        subPackages: [],
                        attributes: [
                            {
                                name: 'amount',
                                label: 'Amount',
                                rangeURI: 'int',
                                required: true
                            }, {
                                name: 'units',
                                label: 'Units',
                                rangeURI: 'string',
                                required: true

                            }, {
                                name: 'kit_type',
                                label: 'Kit',
                                rangeURI: 'string',
                                required: true
                            }
                        ]
                    }
                }, {
                    jsonData: {
                        id: 818,
                        description: 'Creatinine blood test',
                        active: true,
                        repeatable: true,
                        narrative: 'Creatinine: {amount} {units} measured using {kit_type} test',
                        categories: [],
                        subPackages: [],
                        attributes: [
                            {
                                name: 'amount',
                                label: 'Amount',
                                rangeURI: 'int',
                                required: true
                            }, {
                                name: 'units',
                                label: 'Units',
                                rangeURI: 'string',
                                required: true

                            }, {
                                name: 'kit_type',
                                label: 'Kit',
                                rangeURI: 'string',
                                required: true
                            }
                        ]
                    }
                }, {
                    jsonData: {
                        id: 819,
                        description: 'Blood Draw',
                        active: true,
                        repeatable: true,
                        narrative: '{amount} {units} of blood drawn in {tube} tube.',
                        categories: [],
                        subPackages: [],
                        attributes: [
                            {
                                name: 'amount',
                                label: 'Amount',
                                rangeURI: 'int',
                                required: true,
                                validators:[{
                                    expression:"~gte=1&~lte=300",
                                    name:"SND Range",
                                    description:"SND Numeric Range",
                                    type:"range"}]
                            }, {
                                name: 'units',
                                label: 'Units',
                                rangeURI: 'string',
                                required: true

                            }, {
                                name: 'tube',
                                label: 'Tube',
                                rangeURI: 'string',
                                required: true
                            }
                        ]
                    }
                }, {
                    jsonData: {
                        id: 820,
                        description: 'Basic Metabolic Panel',
                        active: true,
                        repeatable: true,
                        narrative: 'Basic Metabolic Panel',
                        categories: [],
                        subPackages: [{
                            pkgId: 814,
                            sortOrder: 1
                        }, {
                            pkgId: 815,
                            sortOrder: 2
                        }, {
                            pkgId: 816,
                            sortOrder: 3
                        }, {
                            pkgId: 817,
                            sortOrder: 4
                        }, {
                            pkgId: 818,
                            sortOrder: 5
                        }, {
                            pkgId: 819,
                            sortOrder: 6
                        }],
                        attributes: []
                    }
                }
            ],
            INIT_PROJECTS: [
                {
                    jsonData: {
                        projectId: 60,
                        active: true,
                        description: "Blood Analysis",
                        referenceId: 120,
                        startDate: "2018-02-26",
                        projectItems: [{
                            pkgId: 814,
                            active: true
                        }, {
                            pkgId: 819,
                            active: true
                        }, {
                            pkgId: 820,
                            active: true
                        }
                        ]
                    }
                },{

                    jsonData:{
                        projectId:61,
                        active:true,
                        description:"Testing the framework",
                        referenceId:121,
                        startDate:"2018-01-27",
                        projectItems: [{
                            pkgId: 814,
                            active: true
                        }
                        ]
                    }
                }
            ]
        }
    };


    function getEventTestData() {
        if (!LABKEY.SND_PKG_CACHE || !LABKEY.SND_PKG_CACHE.loaded) {
            return [];
        }

        return [
            {
                name: 'Valid Save Event: Use Property Names',
                jsonData: {
                    eventId: 1800000,
                    subjectId: 1,
                    date: "2018-02-26T17:51:20",
                    note: "This is a test event note.",
                    projectIdRev: '60|0',
                    eventData: [
                        {
                            superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                            attributes: [{
                                propertyName: 'amount',
                                value: '10'
                            }, {
                                propertyName: 'units',
                                value: "mL"
                            }, {
                                propertyName: 'tube',
                                value: 'red'
                            }]
                        },
                        {
                            superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                            attributes: [{
                                propertyName: 'amount',
                                value: '11'
                            }, {
                                propertyName: 'units',
                                value: "mL"
                            }, {
                                propertyName: 'tube',
                                value: 'red'
                            }]
                        }
                    ]
                },
                expected: {
                    eventId: 1800000,
                    subjectId: "1",
                    date: "2018-02-26T17:51:20",
                    note: "This is a test event note.",
                    projectIdRev: '60|0',
                    eventData: [
                        {
                            superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                            narrative: LABKEY.SND_PKG_CACHE['819']['narrative'],
                            attributes: [{
                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['propertyId'],
                                propertyName: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['name'],
                                value: '10'
                            }, {
                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['propertyId'],
                                propertyName: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['name'],
                                value: "mL"
                            }, {
                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['propertyId'],
                                propertyName: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['name'],
                                value: 'red'
                            }]
                        },
                        {
                            superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                            narrative: LABKEY.SND_PKG_CACHE['819']['narrative'],
                            attributes: [{
                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['propertyId'],
                                propertyName: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'amount')['name'],
                                value: '11'
                            }, {
                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['propertyId'],
                                propertyName: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'units')['name'],
                                value: "mL"
                            }, {
                                propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['propertyId'],
                                propertyName: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['819']['attributes'], 'tube')['name'],
                                value: 'red'
                            }]
                        }
                    ]
                }
            },{
                name :'Valid Save Event with super package',
                jsonData: {
                    eventId: 1800001,
                    subjectId: 2,
                    date: "2018-02-26T17:51:20",
                    note: "Note for event attribute data sample JSON",
                    projectIdRev: '61|0',
                    eventData: [
                        {
                            superPkgId: LABKEY.SND_PKG_CACHE['814']['superPkgId'],
                            attributes : [],
                            subPackages : [{
                                superPkgId : LABKEY.getSubpackageSuperPkgId(810, LABKEY.SND_PKG_CACHE['814']['subPackages']),
                                attributes: [
                                    {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['810']['attributes'], 'amount')['propertyId'],
                                        value: 100
                                    }, {
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
                                        value: 200
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['811']['attributes'], 'units')['propertyId'],
                                        value: "mEq/L"
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['811']['attributes'], 'kit_type')['propertyId'],
                                        value: "Potassium Detection Kit"
                                    }]

                            }, {
                                superPkgId: LABKEY.getSubpackageSuperPkgId(812, LABKEY.SND_PKG_CACHE['814']['subPackages']),
                                attributes: [
                                    {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['812']['attributes'], 'amount')['propertyId'],
                                        value: 300
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['812']['attributes'], 'units')['propertyId'],
                                        value: "mEq/L"
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['812']['attributes'], 'kit_type')['propertyId'],
                                        value: "Carbon Dioxide (CO2) Colorimetric Detection Kit"
                                    }]

                            }, {
                                superPkgId: LABKEY.getSubpackageSuperPkgId(813, LABKEY.SND_PKG_CACHE['814']['subPackages']),
                                attributes: [
                                    {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'amount')['propertyId'],
                                        value: 400
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'units')['propertyId'],
                                        value: "mg/dL"
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'kit_type')['propertyId'],
                                        value: "Chloride Blood Detection Kit"
                                    }]
                            }]
                        }
                    ]
                },
                expected : {
                    eventId: 1800001,
                    subjectId: "2",
                    date: "2018-02-26T17:51:20",
                    note: "Note for event attribute data sample JSON",
                    projectIdRev: '61|0',
                    eventData: [
                        {
                            superPkgId: LABKEY.SND_PKG_CACHE['814']['superPkgId'],
                            attributes : [],
                            subPackages : [{
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

                            }, {
                                superPkgId : LABKEY.getSubpackageSuperPkgId(810, LABKEY.SND_PKG_CACHE['814']['subPackages']),
                                attributes: [
                                    {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['810']['attributes'], 'amount')['propertyId'],
                                        value: "100"
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['810']['attributes'], 'units')['propertyId'],
                                        value: "mEq/L"
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['810']['attributes'], 'kit_type')['propertyId'],
                                        value: "Sodium Colorimetric Detection Kit"
                                    }]

                            }, {
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

                            }, {
                                superPkgId: LABKEY.getSubpackageSuperPkgId(813, LABKEY.SND_PKG_CACHE['814']['subPackages']),
                                attributes: [
                                    {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'amount')['propertyId'],
                                        value: "200"  // Unit conversion in trigger script
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'units')['propertyId'],
                                        value: "mEq/L"  // Unit conversion in trigger script
                                    }, {
                                        propertyId: LABKEY.getAttributeByName(LABKEY.SND_PKG_CACHE['813']['attributes'], 'kit_type')['propertyId'],
                                        value: "Chloride Blood Detection Kit"
                                    }]
                            }]
                        }
                    ]
                }
            }

        ];
    }

    var SNDTEST_CLEAN_DATA = {
        EVENTIDS: [1800000,1800001,1800002]
    };

     LABKEY.getInitData = function () {return SNDTEST_INIT_DATA};
     LABKEY.getCleanData = function () {return SNDTEST_CLEAN_DATA};
     LABKEY.getEventTestData = function () {return getEventTestData()};

})(jQuery);