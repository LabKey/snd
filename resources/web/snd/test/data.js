
(function($) {

    var SNDTEST_INIT_DATA = {
        BEFORE_ALL_TESTS: {
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
                        id: 814,
                        description: 'Elecrolyte Tests',
                        active: true,
                        repeatable: true,
                        narrative: 'Electrolytes',
                        categories: [],
                        subPackages: [{
                            pkgId: 810,
                            sortOrder: 1
                        }, {
                            pkgId: 811,
                            sortOrder: 2
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
                                required: true
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
                }
            ]
        }
    };


    function getEventTests() {
        if (!LABKEY.SND_PKG_CACHE || !LABKEY.SND_PKG_CACHE.loaded) {
            return [];
        }

        return [
            {
                name: 'Valid Save Event',
                jsonData: {
                    eventId: 1800000,
                    participantId: 1,
                    date: "2018-02-26T17:51:20",
                    note: "This is a test event note.",
                    projectIdRev: '60|0',
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
                },
                expected: {
                    eventId: 1800000,
                    participantId: 1,
                    date: "2018-02-26T17:51:20",
                    note: "This is a test event note.",
                    projectIdRev: '60|0',
                    eventData: [
                        {
                            superPkgId: LABKEY.SND_PKG_CACHE['819']['superPkgId'],
                            narrative: LABKEY.SND_PKG_CACHE['819']['narrative'],
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
            },{
                name: 'Invalid Save Event: Missing ParticipantId',
                jsonData: {
                    eventId: 1800001,
                    note: "This is a test event note.",
                    projectIdRev: '60|0',
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
                },
                expectedFailure: "Missing required json parameter: participantId."
            }
        ];
    }

    var SNDTEST_CLEAN_DATA = {
        EVENTIDS: [1800000]
    };

     LABKEY.getInitData = function () {return SNDTEST_INIT_DATA};
     LABKEY.getCleanData = function () {return SNDTEST_CLEAN_DATA};
     LABKEY.getEventTests = function () {return getEventTests()};

})(jQuery);