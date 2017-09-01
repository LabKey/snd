/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.labkey.test.tests.snd;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.Connection;
import org.labkey.remoteapi.query.DeleteRowsCommand;
import org.labkey.remoteapi.query.Filter;
import org.labkey.remoteapi.query.InsertRowsCommand;
import org.labkey.remoteapi.query.SaveRowsResponse;
import org.labkey.remoteapi.query.SelectRowsCommand;
import org.labkey.remoteapi.query.SelectRowsResponse;
import org.labkey.remoteapi.query.UpdateRowsCommand;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.CustomModules;
import org.labkey.test.components.CustomizeView;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.Maps;
import org.labkey.test.util.SqlserverOnlyTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Category ({CustomModules.class})
public class SNDTest extends BaseWebDriverTest implements SqlserverOnlyTest
{
    {setIsBootstrapWhitelisted(true);}
    private static final String PROJECTNAME = "SNDTest Project";
    private static final String TEST1SUBFOLDER = "Test1";
    private static final String TEST1PATH = PROJECTNAME + "/" + TEST1SUBFOLDER;
    private static final String PKGSTESTCOL = "testPkgs";
    private static final String EXTCOLTESTDATA1 = "testString 1";
    private static final String EXTCOLTESTDATA2 = "testString 2";
    private static final String EXTCOLTESTDATA3 = "testString 3";
    private static final String EXTCOLTESTDATA3A = "updated testString 3";

    private static final String CREATEDOMAINSAPI ="LABKEY.Domain.create({\n" +
            "   domainGroup: \"test\",\n" +
            "   domainKind: \"SND\",\n" +
            "   module: \"snd\",\n" +
            "   importData: false,\n" +
            "   success: onSuccess,\n" +
            "   failure: onFailure\n" +
            "});\n" +
            "function onFailure(e)\n" +
            "{\n" +
            "   callback(e.exception);\n" +
            "}\n" +
            "\n" +
            "function onSuccess()\n" +
            "{\n" +
            "   callback('Success!');\n" +
            "}\n";


    private static final String SAVEPACKAGEAPI = "LABKEY.Ajax.request({ \n" +
"    method: 'POST',                                                                                     \n" +
"            url: LABKEY.ActionURL.buildURL('snd', 'savePackage.api'),                                   \n" +
"            success: function(){ callback('Success!'); },                                               \n" +
"    failure: function(e){ callback(e.responseText); },                                                  \n" +
"    jsonData: {                                                                                         \n" +
"        //'id': 10027,                                                                                  \n" +
"        'description': 'My package description2',                                                       \n" +
"                'active': true,                                                                         \n" +
"                'repeatable': true,                                                                     \n" +
"                'narrative': 'This is a narrative',                                                     \n" +
"                'categories': [102, 103],                                                               \n" +
"        'subpackages': [],                                                                              \n" +
"        'extraFields': {'UsdaCode':'B'},                                                                \n" +
"        'attributes': [{                                                                                \n" +
"            'name': 'SNDName',                                                                          \n" +
"                    'label': 'Name',                                                                    \n" +
"                   'rangeURI': 'http://www.w3.org/2001/XMLSchema#string',                               \n" +
"                    'required': false,                                                                  \n" +
"                    'scale': 500,                                                                       \n" +
"                    'validators': [{                                                                    \n" +
"                'name': 'SNDLength',                                                                    \n" +
"                        'description': 'This will check the length of the field',                       \n" +
"                        'type': 'length',                                                               \n" +
"                        'expression': '~gte=1&amp;~lte=400',                                            \n" +
"                        'errorMessage': 'This value must be between 1 and 400 characters long'          \n" +
"            }]                                                                                          \n" +
"        },{                                                                                             \n" +
"            'name': 'SNDUser',                                                                          \n" +
"                    'label': 'User',                                                                    \n" +
"                   'rangeURI': 'http://www.w3.org/2001/XMLSchema#int',                                  \n" +
"                    'required': true,                                                                   \n" +
"                    'lookupSchema': 'core',                                                             \n" +
"                    'lookupQuery': 'Principals'                                                         \n" +
"        },{                                                                                             \n" +
"            'name': 'SNDAge',                                                                           \n" +
"                    'label': 'Age',                                                                     \n" +
"                   'rangeURI': 'http://www.w3.org/2001/XMLSchema#double',                               \n" +
"                    'format': '0.##',                                                                   \n" +
"                    'validators': [{                                                                    \n" +
"                'name': 'SNDRange',                                                                     \n" +
"                        'description': 'This will check the range of the field',                        \n" +
"                        'type': 'range',                                                                \n" +
"                        'expression': '~gte=1&amp;~lte=100',                                            \n" +
"                        'errorMessage': 'No centenarians allowed'                                       \n" +
"            }]                                                                                          \n" +
"        }]                                                                                              \n" +
"    }                                                                                                   \n" +
"})";

    private static final String GETPACKAGEAPI = "LABKEY.Ajax.request({  \n" +
            "                    method: 'POST',                                                   \n" +
            "    url: LABKEY.ActionURL.buildURL('snd', 'getPackages.api'),                         \n" +
            "    success: function(data, a, b, c, d){                                              \n" +
            "                        callback(JSON.stringify(JSON.parse(data.response).json[0]));  \n" +
            "},                                                                                    \n" +
            "    failure: function(e){ callback(e.responseText); },                                \n" +
            "    jsonData: {'packages':['10001']}                                                  \n" +
            "});";

    private static final String ADDEVENT = "" +
            "	LABKEY.Query.insertRows({                                                                                                             " +
            "		containerPath: '" + PROJECTNAME + "',                                                                                                         " +
            "		schemaName: 'snd',                                                                                                               " +
            "		queryName: 'SuperPkgs',                                                                                                                 " +
            "		rows:                                                                                                                        " +
            "        [{                                                       \n" +
            "    'PkgId': 10001,                                              \n" +
            "    'SuperPkgId': 10001,                                         \n" +
            "    'SuperPkgPath': 'test'                                       \n" +
            "}],                                                              \n" +
            "    successCallback: function(data){                             \n" +
            "        createEvent(10001);                                      \n" +
            "    },                                                           \n" +
            "    failureCallback: function(e){                                \n" +
            "        callback(e.exception);                                   \n" +
            "    }                                                            \n" +
            "	});                                                           \n"+
            "function createEvent(id){" +
            "    LABKEY.Query.insertRows({                                    \n" +
            "            containerPath: '" + PROJECTNAME + "',                \n" +
            "            schemaName: 'snd',                                   \n" +
            "            queryName: 'Events',                                 \n" +
            "            rows: [{                                             \n" +
            "        'EventId': id,                                           \n" +
            "                'Id': 1,"                                            +
            "                'Date': new Date()                               \n" +
            "    }],                                                          \n" +
            "    successCallback: function(data){                             \n" +
            "        createCodedEvent(id);                                    \n" +
            "    },                                                           \n" +
            "    failureCallback: function(e){                                \n" +
            "        callback(e.exception);                                   \n" +
            "    }                                                            \n" +
            "	});"                                                              +
            "}                                                                \n" +
            "function createCodedEvent(id){" +
            "    LABKEY.Query.insertRows({                                    \n" +
            "            containerPath: '" + PROJECTNAME + "',                \n" +
            "            schemaName: 'snd',                                   \n" +
            "            queryName: 'CodedEvents',                            \n" +
            "            rows: [{                                             \n" +
            "        'EventId': id,                                           \n" +
            "                'SuperPkgId': id                                 \n" +
            "    }],                                                          \n" +
            "    successCallback: function(data){                             \n" +
            "        callback('Success!');                                    \n" +
            "    },                                                           \n" +
            "    failureCallback: function(e){                                \n" +
            "        callback(e.exception);                                   \n" +
            "    }                                                            \n" +
            "	});                                                           \n" +
            "}                                                                \n"
;
    private static final String ADDPACKAGETOPROJECT ="" +
            "	LABKEY.Query.insertRows({                                                                                                             "+
            "		containerPath: '"+ PROJECTNAME + "',                                                                                                         "+
            "		schemaName: 'snd',                                                                                                               "+
            "		queryName: 'Projects',                                                                                                                 "+
            "		rows:                                                                                                                        "+
            "                [{                                                 \n" +
            "                'ProjectId': 10001,                                \n" +
            "        'RevisionNum': 1,                                          \n" +
            "        'ReferenceId': 1,                                          \n" +
            "        'StartDate': new Date(),                                   \n" +
            "			'Description': 'Description for package-' + 10001,      \n" +
            "        'ObjectId': '657b0012-c94e-4cfb-b4a7-499a57c' + 10001      \n" +
            "}],                                                                \n" +
            "    successCallback: function(data){                               \n" +
            "        createProjectItem(10001);                                  \n" +
            "    },                                                             \n" +
            "    failureCallback: function(e){                                  \n" +
            "        callback(e.exception);                                     \n" +
            "    }                                                              \n" +
            "    });                                                            \n" +
            "                                                                   \n" +
            "                                                                   \n" +
            "        function createProjectItem(id) {                           \n" +
            "	LABKEY.Query.insertRows({                                        "+
            "		containerPath: '"+ PROJECTNAME + "',                         "+
            "		schemaName: 'snd',                                           "+
            "		queryName: 'ProjectItems',                                   "+
            "		rows:                                                        "+
            "        [{                                                          \n" +
        "            'ProjectItemId': id,                                        \n" +
        "            'ParentObjectId': '657b0012-c94e-4cfb-b4a7-499a57c' + id,   \n" +
        "            'SuperPkgId': id,                                           \n" +
        "            'Active': true                                              \n" +
            "}],                                                                 \n" +
        "    successCallback: function(data){                                    \n" +
        "        callback('Success!');                                           \n" +
        "    },                                                                  \n" +
        "    failureCallback: function(e){                                       \n" +
        "        callback(e.exception);     \n" +
        "    }                                                                 \n" +
        "	});                                                                 \n" +
        "}                                                                     \n" +
        "                                                                      \n";

    // The concept here was to include the entire set of scripts and call each as a test.
    // It was a challenge to debug the js issues but may still be worth pursing.
    private static final String APISCRIPTS =
        "var container = '"+ PROJECTNAME +"';                                                                                                     "+
        "                                                                                                                                         "+
        "function populateCategories() {                                                                                                          "+
        "	LABKEY.Query.insertRows({                                                                                                             "+
        "             containerPath: container,                                                                                                   "+
        "             schemaName: 'snd',                                                                                                          "+
        "             queryName: 'PkgCategories',                                                                                                 "+
        "             rows: [{                                                                                                                    "+
        "				'CategoryId':  100,                                                                                                       "+
        "				'Description':  'Surgery',                                                                                                "+
        "				'Active': true,                                                                                                           "+
        "				'Comment': 'This is a surgery'                                                                                            "+
        "				},{                                                                                                                       "+
        "				'CategoryId':  101,                                                                                                       "+
        "				'Description':  'Blood Draw',                                                                                             "+
        "				'Active': true,                                                                                                           "+
        "				'Comment': 'This is a blood draw'                                                                                         "+
        "				},{                                                                                                                       "+
        "				'CategoryId':  102,                                                                                                       "+
        "				'Description':  'Weight',                                                                                                 "+
        "				'Active': true,                                                                                                           "+
        "				'Comment': 'This is a weight'                                                                                             "+
        "				},{                                                                                                                       "+
        "				'CategoryId':  103,                                                                                                       "+
        "				'Description':  'Vitals',                                                                                                 "+
        "				'Active': true,                                                                                                           "+
        "				'Comment': 'This is vitals'                                                                                               "+
        "			}],                                                                                                                           "+
        "			successCallback: function(data){                                                                                              "+
        "				callback('Success!');                                                            "+
        "			},                                                                                                                            "+
        "			failureCallback: function(e){                                                                                                 "+
        "				callback(e.exception);                                                                                     "+
        "			}                                                                                                                             "+
        "	});                                                                                                                                   "+
        "}                                                                                                                                        "+
        "                                                                                                                                         ";

    //        "function myGetPackage() {                                                                                                                "+
//        "	var package = prompt('Enter package id');                                                                                             "+
//        "                                                                                                                                         "+
//        "	if (package != null || package != '') {                                                                                               "+
//        "		LABKEY.Ajax.request({                                                                                                             "+
//        "			method: 'POST',                                                                                                               "+
//        "			url: LABKEY.ActionURL.buildURL('snd', 'getPackages.api'),                                                                     "+
//        "			success: function(data, a, b, c, d){                                                                                          "+
//        "				//Ext4.Msg.alert('Success',JSON.stringify(JSON.parse(data.response).json[0]));                                            "+
//        "				codeMirrorWindow('Package JSON', JSON.stringify(JSON.parse(data.response).json[0]), 'application/json');                  "+
//        "			},                                                                                                                            "+
//        "			failure: function(e){ Ext4.Msg.alert('Failure',e.responseText); },                                                            "+
//        "			jsonData: {'packages':[package]}                                                                                              "+
//        "		});                                                                                                                               "+
//        "	}                                                                                                                                     "+
//        "}                                                                                                                                        "+
//        "                                                                                                                                         "+
//        "function mySavePackage() {                                                                                                               "+
//        "	LABKEY.Ajax.request({                                                                                                                 "+
//        "		method: 'POST',                                                                                                                   "+
//        "		url: LABKEY.ActionURL.buildURL('snd', 'savePackage.api'),                                                                         "+
//        "		success: function(){ callback('Success!'); },                                                   "+
//        "		failure: function(e){ callback(e.responseText); },                                                                "+
//        "		jsonData: {                                                                                                                       "+
//        "			//'id': 10001,                                                                                                                "+
//        "			'description': 'My package description2',                                                                                     "+
//        "			'active': true,                                                                                                               "+
//        "			'repeatable': true,                                                                                                           "+
//        "			'narrative': 'This is a narrative',                                                                                           "+
//        "			'categories': [102, 103],                                                                                                     "+
//        "			'subpackages': [],                                                                                                            "+
//        "			'extraFields': {'UsdaCode':'B'},                                                                                              "+
//        "			'attributes': [{                                                                                                              "+
//        "				'name': 'SNDName',                                                                                                        "+
//        "				'label': 'Name',                                                                                                          "+
//        "				'rangeURI': 'http://www.w3.org/2001/XMLSchema#string',                                                                    "+
//        "				'required': false,                                                                                                        "+
//        "				'scale': 500,                                                                                                             "+
//        "				'validators': [{                                                                                                          "+
//        "					'name': 'SNDLength',                                                                                                  "+
//        "					'description': 'This will check the length of the field',                                                             "+
//        "					'type': 'length',                                                                                                     "+
//        "					'expression': '~gte=1&amp;~lte=400',                                                                                  "+
//        "					'errorMessage': 'This value must be between 1 and 400 characters long'                                                "+
//        "				}]                                                                                                                        "+
//        "			},{                                                                                                                           "+
//        "				'name': 'SNDUser',                                                                                                        "+
//        "				'label': 'User',                                                                                                          "+
//        "				'rangeURI': 'http://www.w3.org/2001/XMLSchema#int',                                                                       "+
//        "				'required': true,                                                                                                         "+
//        "				'lookupSchema': 'core',                                                                                                   "+
//        "				'lookupQuery': 'Principals'                                                                                               "+
//        "			},{                                                                                                                           "+
//        "				'name': 'SNDAge',                                                                                                         "+
//        "				'label': 'Age',                                                                                                           "+
//        "				'rangeURI': 'http://www.w3.org/2001/XMLSchema#double',                                                                    "+
//        "				'format': '0.##',                                                                                                         "+
//        "				'validators': [{                                                                                                          "+
//        "					'name': 'SNDRange',                                                                                                   "+
//        "					'description': 'This will check the range of the field',                                                              "+
//        "					'type': 'range',                                                                                                      "+
//        "					'expression': '~gte=1&amp;~lte=100',                                                                                  "+
//        "					'errorMessage': 'No centenarians allowed'                                                                             "+
//        "				}]                                                                                                                        "+
//        "			}]                                                                                                                            "+
//        "		}                                                                                                                                 "+
//        "	});                                                                                                                                   "+
//        "}                                                                                                                                        ";

    private static final String CREATECATEGORIESAPI = APISCRIPTS + " populateCategories();";
//    private static final String SAVEPACKAGEAPI = APISCRIPTS + " mySavePackage():";


    private final Map<String, Object> TEST1ROW1MAP = Maps.of("PkgId", 1001, "Description", "Description 1", "ObjectId", "dbe961b9-b7ba-102d-8c2a-99223451b901", "testPkgs", EXTCOLTESTDATA1);
    private final Map<String, Object> TEST1ROW2MAP = Maps.of("PkgId", 1002, "Description", "Description 2", "ObjectId", "dbe961b9-b7ba-102d-8c2a-99223751b901", "testPkgs", EXTCOLTESTDATA2);
    private final Map<String, Object> TEST1ROW3MAP = Maps.of("PkgId", 1003, "Description", "Description 3", "ObjectId", "dbe961b9-b7ba-102d-8c2a-99223481b901", "testPkgs", EXTCOLTESTDATA3);
    private final Map<String, Object> TEST1ROW3AMAP = Maps.of("PkgId", 1003, "Description", "Updated Description 3", "ObjectId", "dbe961b9-b7ba-102d-8c2a-99223481b901", "testPkgs", EXTCOLTESTDATA3A);


    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        _containerHelper.deleteProject(getProjectName(), afterTest);
    }

    // If values exist this will delete them.  Path of project folder must be valid.
    protected void deleteIfNeeded(String path, String schemaName, String queryName, Map<String, Object> map, String pkName) throws IOException, CommandException
    {
        Connection cn = createDefaultConnection(false);

        SelectRowsCommand selectCmd = new SelectRowsCommand(schemaName, queryName);
        selectCmd.addFilter(new Filter(pkName, map.get(pkName)));
        SelectRowsResponse srr = selectCmd.execute(cn, path);

        if (srr.getRowCount().intValue() > 0)
        {
            DeleteRowsCommand deleteCmd = new DeleteRowsCommand(schemaName, queryName);
            deleteCmd.addRow(map);
            deleteCmd.execute(cn, path);
        }
    }

    @LogMethod
    private void deleteTest1Data() throws CommandException, IOException
    {
        if (_containerHelper.doesFolderExist(PROJECTNAME, PROJECTNAME, TEST1SUBFOLDER))
        {
            log("Deleting test 1 data");

            deleteIfNeeded(TEST1PATH, "snd", "Pkgs", TEST1ROW1MAP, "PkgId");
            deleteIfNeeded(TEST1PATH, "snd", "Pkgs", TEST1ROW2MAP, "PkgId");
            deleteIfNeeded(TEST1PATH, "snd", "Pkgs", TEST1ROW3MAP, "PkgId");
        }
    }

    @BeforeClass
    public static void setupProject()
    {
        SNDTest init = (SNDTest) getCurrentTest();

        init.doSetup();
    }

    private void doSetup()
    {
        _containerHelper.createProject(getProjectName(), "Collaboration");
        goToProjectHome();
        _containerHelper.enableModules(Arrays.asList("SND"));
        _containerHelper.createSubfolder(getProjectName(), getProjectName(), TEST1SUBFOLDER, "Collaboration", new String[]{"SND"});
        setupTest1Project();
    }

    private void setupTest1Project()
    {
        clickFolder(TEST1SUBFOLDER);
        runScript(CREATEDOMAINSAPI);
    }

    private void runScript(String script)
    {
        String result = (String) executeAsyncScript(script);
        assertEquals(result, "Success!", result);
    }

    @Before
    public void preTest()
    {
        goToProjectHome();
    }

    @Test
    public void testSNDModule()
    {
        //TODO: Implement this once we get a UI
//        BeginPage beginPage = BeginPage.beginAt(this, getProjectName());
//        assertEquals(200, getResponseCode());
//        final String expectedHello = "Hello, and welcome to the SND module.";
//        assertEquals("Wrong hello message", expectedHello, beginPage.getHelloMessage());
    }

    @Override
    protected BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Override
    protected String getProjectName()
    {
        return PROJECTNAME;
    }

    @Override
    public List<String> getAssociatedModules()
    {
        return Collections.singletonList("SND");
    }

    @Test
    public void testExtensibleColumns() throws Exception
    {
        clickFolder(TEST1SUBFOLDER);

        Connection cn = createDefaultConnection(false);

        InsertRowsCommand insertRowsCommand = new InsertRowsCommand("snd", "Pkgs");
        insertRowsCommand.addRow(TEST1ROW1MAP);
        insertRowsCommand.addRow(TEST1ROW2MAP);
        insertRowsCommand.addRow(TEST1ROW3MAP);
        SaveRowsResponse resp = insertRowsCommand.execute(cn, getProjectName() + "/" + TEST1SUBFOLDER);
        assertEquals(resp.getRowsAffected().intValue(), 3);

        goToSchemaBrowser();
        selectQuery("snd", "Pkgs");
        assertTextPresent(PKGSTESTCOL);
        waitAndClickAndWait(Locator.linkWithText("view data"));

        CustomizeView customizeViewHelper = new CustomizeView(this);

        customizeViewHelper.openCustomizeViewPanel();
        customizeViewHelper.addColumn("testPkgs");
        customizeViewHelper.clickViewGrid();
        waitForText("This grid view has been modified.");

        assertTextPresent(EXTCOLTESTDATA1, EXTCOLTESTDATA2, EXTCOLTESTDATA3);

        clickFolder(TEST1SUBFOLDER);

        UpdateRowsCommand updateRowsCommand = new UpdateRowsCommand("snd", "Pkgs");
        updateRowsCommand.addRow(TEST1ROW3AMAP);
        resp = updateRowsCommand.execute(cn, getProjectName() + "/" + TEST1SUBFOLDER);
        assertEquals(resp.getRowsAffected().intValue(), 1);

        goToSchemaBrowser();
        selectQuery("snd", "Pkgs");
        waitAndClickAndWait(Locator.linkWithText("view data"));
        assertTextPresent(EXTCOLTESTDATA1, EXTCOLTESTDATA2, EXTCOLTESTDATA3A, "Updated Description 3");

        clickFolder(TEST1SUBFOLDER);

        DeleteRowsCommand deleteRowsCommand = new DeleteRowsCommand("snd", "Pkgs");
        deleteRowsCommand.addRow(TEST1ROW2MAP);
        resp = deleteRowsCommand.execute(cn, getProjectName() + "/" + TEST1SUBFOLDER);
        assertEquals(resp.getRowsAffected().intValue(), 1);

        goToSchemaBrowser();
        selectQuery("snd", "Pkgs");
        waitAndClickAndWait(Locator.linkWithText("view data"));
        assertTextNotPresent(EXTCOLTESTDATA2, "Description 2");
    }

    @Test
    public void testPackageApis()
    {   DataRegionTable dataRegionTable;
        //insert package categories
        runScript(CREATECATEGORIESAPI);
        goToProjectHome();
        goToSchemaBrowser();
        dataRegionTable = viewQueryData("snd", "PkgCategories");
        assertEquals("Wrong count of categories",4,dataRegionTable.getDataRowCount());

        //insert package
        runScript(SAVEPACKAGEAPI);
        goToSchemaBrowser();
        dataRegionTable = viewQueryData("snd", "Pkgs");
        assertEquals("Wrong count of categories",1,dataRegionTable.getDataRowCount());

        //get package json
        String result = (String) executeAsyncScript(GETPACKAGEAPI);
        JSONObject resultAsJson = new JSONObject(result);
        assertEquals("Wrong narrative","This is a narrative", resultAsJson.getString("narrative"));

        JSONArray attributes = resultAsJson.getJSONArray("attributes");
        assertEquals("Wrong attribute count",3,attributes.length());

        JSONArray categories = resultAsJson.getJSONArray("categories");
        assertEquals("Wrong category count",2,categories.length());
        assertEquals("Wrong category ",102,categories.getInt(0));
        assertEquals("Wrong category ",103,categories.getInt(1));


        JSONArray validators = attributes.getJSONObject(0).getJSONArray("validators");
        assertEquals("Wrong validator count",1,validators.length());

        //confirm package currently has no event
        goToSchemaBrowser();
        dataRegionTable = viewQueryData("snd", "Pkgs");
        assertEquals("Has event not false","false",dataRegionTable.getDataAsText(0,"Has Event"));
        assertEquals("Has project not false","false",dataRegionTable.getDataAsText(0,"Has Project"));

        //create event
        runScript(ADDEVENT);
        goToSchemaBrowser();
        dataRegionTable = viewQueryData("snd", "Pkgs");
        assertEquals("Has event not true","true",dataRegionTable.getDataAsText(0,"Has Event"));
        assertEquals("Has project not false","false",dataRegionTable.getDataAsText(0,"Has Project"));

        //add package to project
        runScript(ADDPACKAGETOPROJECT);
        goToSchemaBrowser();
        dataRegionTable = viewQueryData("snd", "Pkgs");
        assertEquals("Has event not true","true",dataRegionTable.getDataAsText(0,"Has Event"));
        assertEquals("Has project not true","true",dataRegionTable.getDataAsText(0,"Has Project"));
    }
}