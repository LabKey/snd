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
import org.junit.Ignore;
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
import org.labkey.remoteapi.query.TruncateTableCommand;
import org.labkey.remoteapi.query.UpdateRowsCommand;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.TestFileUtils;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.WebDriverWrapper;
import org.labkey.test.categories.CustomModules;
import org.labkey.test.components.CustomizeView;
import org.labkey.test.components.bootstrap.ModalDialog;
import org.labkey.test.components.snd.AttributeGridRow;
import org.labkey.test.components.snd.AttributesGrid;
import org.labkey.test.components.snd.CategoryEditRow;
import org.labkey.test.components.snd.FilterSelect;
import org.labkey.test.components.snd.PackageViewerResult;
import org.labkey.test.pages.snd.EditCategoriesPage;
import org.labkey.test.pages.snd.EditPackagePage;
import org.labkey.test.pages.snd.PackageListPage;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.Maps;
import org.labkey.test.util.SqlserverOnlyTest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
"        'description': 'My package description',                                                       \n" +
"                'active': true,                                                                         \n" +
"                'repeatable': true,                                                                     \n" +
"                'narrative': 'This is a narrative',                                                     \n" +
"                'categories': [102, 103],                                                               \n" +
"        'subPackages': [],                                                                              \n" +
"        'extraFields': {'UsdaCode':'B'},                                                                \n" +
"        'attributes': [{                                                                                \n" +
"            'name': 'SNDName',                                                                          \n" +
"                    'label': 'Name',                                                                    \n" +
"                    'rangeURI': 'string',                                                               \n" +
"                    'required': false,                                                                  \n" +
"                    'scale': 500,                                                                       \n" +
"                    'validators': [{                                                                    \n" +
"                        'name': 'SNDLength',                                                            \n" +
"                        'description': 'This will check the length of the field',                       \n" +
"                        'type': 'length',                                                               \n" +
"                        'expression': '~gte=1&amp;~lte=400',                                            \n" +
"                        'errorMessage': 'This value must be between 1 and 400 characters long'          \n" +
"            }]                                                                                          \n" +
"        },{                                                                                             \n" +
"            'name': 'SNDUser',                                                                          \n" +
"                    'label': 'User',                                                                    \n" +
"                    'rangeURI': 'int',                                                                  \n" +
"                    'required': true,                                                                   \n" +
"                    'lookupSchema': 'core',                                                             \n" +
"                    'lookupQuery': 'Principals'                                                         \n" +
"        },{                                                                                             \n" +
"            'name': 'SNDAge',                                                                           \n" +
"                    'label': 'Age',                                                                     \n" +
"                    'rangeURI': 'double',                                                               \n" +
"                    'format': '0.##',                                                                   \n" +
"                    'validators': [{                                                                    \n" +
"                        'name': 'SNDRange',                                                             \n" +
"                        'description': 'This will check the range of the field',                        \n" +
"                        'type': 'range',                                                                \n" +
"                        'expression': '~gte=1&amp;~lte=100',                                            \n" +
"                        'errorMessage': 'No centenarians allowed'                                       \n" +
"            }]                                                                                          \n" +
"        }]                                                                                              \n" +
"    }                                                                                                   \n" +
"})";

    private static String getPackageWithId(String packageId)
    {
        return "LABKEY.Ajax.request({  \n" +
                "                    method: 'POST',                                                   \n" +
                "    url: LABKEY.ActionURL.buildURL('snd', 'getPackages.api'),                         \n" +
                "    success: function(data, a, b, c, d){                                              \n" +
                "                        callback(JSON.stringify(JSON.parse(data.response).json[0]));  \n" +
                "},                                                                                    \n" +
                "    failure: function(e){ callback(e.responseText); },                                \n" +
                "    jsonData: {'packages':['" + packageId + "']}                                      \n" +
                "});";
    }

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

    private String addCategoryScript(String container, String description, String comment, boolean active, int id)
    {
        String isActive = active ? "true":"false";
        return "LABKEY.Query.insertRows({                                               "+
                "             containerPath: "+container+",                             "+
                        "             schemaName: 'snd',                                "+
                        "             queryName: 'PkgCategories',                       "+
                        "             success: callback,"+
                        "             failure: callback,"+
                        "             rows: [{                                          "+
                        "				'CategoryId':  "+Integer.toString(id)+",        "+
                        "				'Description':  '"+description+"',              "+
                        "				'Active': "+ isActive   +",                     "+
                        "				'Comment': '"+comment+"'                        "+
                        "				}]);";
    }

    private static final String CREATECATEGORIESAPI = APISCRIPTS + " populateCategories();";
//    private static final String SAVEPACKAGEAPI = APISCRIPTS + " mySavePackage():";

    private final Map<String, Object> TEST1ROW1MAP = Maps.of("PkgId", 9900, "Description", "Description 1", "ObjectId", "dbe961b9-b7ba-102d-8c2a-99223451b901", "testPkgs", EXTCOLTESTDATA1);
    private final Map<String, Object> TEST1ROW2MAP = Maps.of("PkgId", 9901, "Description", "Description 2", "ObjectId", "dbe961b9-b7ba-102d-8c2a-99223751b901", "testPkgs", EXTCOLTESTDATA2);
    private final Map<String, Object> TEST1ROW3MAP = Maps.of("PkgId", 9902, "Description", "Description 3", "ObjectId", "dbe961b9-b7ba-102d-8c2a-99223481b901", "testPkgs", EXTCOLTESTDATA3);
    private final Map<String, Object> TEST1ROW3AMAP = Maps.of("PkgId", 9902, "Description", "Updated Description 3", "ObjectId", "dbe961b9-b7ba-102d-8c2a-99223481b901", "testPkgs", EXTCOLTESTDATA3A);

    //Sample files for import to be used in this order
    private static final File INITIAL_IMPORT_FILE = TestFileUtils.getSampleData("snd/pipeline/import/1_initial.snd.xml");
    private static final File CHANGE_NARRATIVE_FILE = TestFileUtils.getSampleData("snd/pipeline/import/2_changeNarrative.snd.xml");
    private static final File INSERT_PACKAGE_FILE = TestFileUtils.getSampleData("snd/pipeline/import/3_insertPackage.snd.xml");
    private static final File ADD_ATTRIBUTE_FILE = TestFileUtils.getSampleData("snd/pipeline/import/4_addAttribute.snd.xml");
    private static final File REMOVE_ATTRIBUTE_FILE= TestFileUtils.getSampleData("snd/pipeline/import/5_removeAttribute.snd.xml");
    private static final File REMOVE_ALL_ATTRIBUTES_FILE = TestFileUtils.getSampleData("snd/pipeline/import/6_removeAllAttributes.snd.xml");

    private static final int IMPORT_WAIT_TIME = 30 * 1000;  // limit of 30 secs
    private int EXPECTED_IMPORT_JOBS = 1;

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

        //insert package categories
        runScript(CREATECATEGORIESAPI);

        // this needs to run first; it has hard-coded IDs
        testPackageApis();
    }

    private void setupTest1Project()
    {
        clickFolder(TEST1SUBFOLDER);
        runScript(CREATEDOMAINSAPI);
    }

    private void runScript(String script)
    {
        String result = (String) executeAsyncScript(script);
        assertEquals("JavaScript API failure.", "Success!", result);
    }

    @Before
    public void preTest() throws Exception
    {
        goToProjectHome();
//        truncateSndPkg();

        //TODO: once exp tables are exposed - do a full cleanup from snd.pkgs, exp.DomainDescriptor, exp.PropertyDomain, exp.PropertyDescriptor
    }

    @Test @Ignore
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
    @Ignore
    public void submitDraftPackageForReview()
    {
        String description = "Our first package draft. Ambitious!";
        String narrative = "Shall I {compare} thee to a summer's day?" +
                "{Thou} art more lovely, and more {temperate};" +
                "Rough {winds} do shake the {darling} buds of May," +
                "And summer's {lease} hath all too short a date";
        PackageListPage listPage = PackageListPage.beginAt(this, getProjectName());
        EditPackagePage editPage = listPage.clickNewPackage();
        editPage.setDescription(description);
        editPage.setNarrative(narrative);

        AttributesGrid grid = editPage.getAttributesGrid();
        grid.waitForRows(6);

        // now edit a row
        AttributeGridRow windsRow = grid.getRow("winds")
                .setDataType("String")
                .setLabel("blustery")
                .setMin(2)
                .setMax(7)
                .setDefault("breeze")
                .setRequired(true)
                .setRedactedText("lol");

        listPage = editPage.clickSaveAsDraft();
        listPage.showDrafts(true);
        listPage.setSearchFilter(description);
        PackageViewerResult packageViewerResult = listPage.getPackage(description);

        EditPackagePage reviewPage = packageViewerResult.clickEdit();
        assertEquals("narrative should equal what we set" ,narrative, reviewPage.getNarrative());
        assertEquals("description should equal what we set" ,description, reviewPage.getDescription());
        reviewPage.clickSubmitForReview();
    }

    @Test
    public void cloneDraftPackage()
    {
        String description = "more sonnets. Ambitious!";
        String narrative = "Sometimes too hot the {eye} of heaven shines" +
                "and often is is {gold} complexion dimm'd," +
                "and every {fair} from {fair} sometime declines," +
                "by chance, or nature's changing {course} untrimmed";
        PackageListPage listPage = PackageListPage.beginAt(this, getProjectName());
        EditPackagePage editPage = listPage.clickNewPackage();
        editPage.setDescription(description);
        editPage.setNarrative(narrative);

        AttributesGrid grid = editPage.getAttributesGrid();
        grid.waitForRows(5);    // should this pass? duplicate token?

        // now edit a row
        AttributeGridRow fairRow = grid.getRow("fair")
                .setDataType("String")
                .setLabel("appearance")
                .setMin(2)
                .setMax(7)
                .setDefault(":-)")
                .setRequired(true)
                .setRedactedText("lol");

        listPage = editPage.clickSaveAsDraft();
        listPage.showDrafts(true);
        listPage.setSearchFilter(description);
        PackageViewerResult packageViewerResult = listPage.getPackage(description);

        EditPackagePage reviewPage = packageViewerResult.clickClone();
        assertEquals("narrative should equal what we set" ,narrative, reviewPage.getNarrative());
        assertEquals("description should equal what we set" ,description, reviewPage.getDescription());
        reviewPage.setDescription("clone of more sonnets narrative.  Ugh, derivative!")
                .setNarrative(narrative + "But thy eternal summer shall not fade, nor lose possession of that fair thou ow'st" +
                        "when in eternal lines to time thou grow'st," +
                        "so long as men can breathe or eyes can see, " +
                        "so long lives this, and this gives life to thee.")
                .clickSaveAsDraft();
    }

    @Test
    public void saveNewPackage()
    {
        String description = "Our first package. Adorable!";
        String narrative = "Four {score} and seven {years} ago, our {forefathers} brought forth," +
                " upon this {continent}, a new {nation}, conceived in liberty, " +
                "and dedicated to the proposition that all {men} are created equal.";
        PackageListPage listPage = PackageListPage.beginAt(this , getProjectName());

        EditPackagePage editPage = listPage.clickNewPackage();
        FilterSelect categoriesSelect = editPage.getCategoriesSelect();
        categoriesSelect
                .selectItem("Surgery")
                .selectItem("Blood Draw")
                .close();

        editPage.setDescription(description);
        editPage.setNarrative(narrative);

        AttributesGrid grid = editPage.getAttributesGrid();
        grid.waitForRows(6);

        // now edit a row
        AttributeGridRow menRow = grid.getRow("men")
                .setDataType("String")
                .setLabel("gender-generic term")
                .setMin(2)
                .setMax(7)
                .setDefault("men")
                .setRequired(true)
                .setRedactedText("and women");
        // move a row Up
        AttributeGridRow nationRow = grid.getRow("nation")
                .selectOrder("Move Down");

        listPage = editPage.clickSave();
        listPage.setSearchFilter(description);
        PackageViewerResult packageViewerResult = listPage.getPackage(description);

        EditPackagePage viewPage = packageViewerResult.clickView();
        assertEquals("narrative should equal what we set" ,narrative, viewPage.getNarrative());
        assertEquals("description should equal what we set" ,description, viewPage.getDescription());

        List<String> selectedCategories = viewPage.getCategoriesSelect().getSelections();
        assertTrue("Expect Blood Draw category", selectedCategories.stream().anyMatch((a)-> a.contains("Blood Draw")));
        assertTrue("Expect Surgery category", selectedCategories.stream().anyMatch((a)-> a.contains("Surgery")));
    }

    @Test
    public void addCategoryViaUI() throws Exception
    {
        String ourNewCategory = "a test category, created via the UI";
        PackageListPage listPage = PackageListPage.beginAt(this , getProjectName());
        EditCategoriesPage catPage = listPage.clickEditCategories();

        // create a new one via the UI
        catPage.addCategory(ourNewCategory, true);
        catPage = catPage.clickSave();
        waitFor(()-> false, 2000);

        SelectRowsCommand catsCmd = new SelectRowsCommand("snd", "PkgCategories");
        SelectRowsResponse afterCats = catsCmd.execute(createDefaultConnection(false), getProjectName());

        assertTrue("our category should have been created, but was not",
                afterCats.getRows().stream().anyMatch((a)-> a.get("Description").equals(ourNewCategory)));
    }

    @Test
    public void deleteCategoryViaUI() throws Exception
    {
        // insert the category via API
        String ourCategory = "a category to be deleted for test purposes";
        InsertRowsCommand cmd = new InsertRowsCommand("snd", "PkgCategories");
        Map<String, Object> catMap = new HashMap<>();
        catMap.put("Description", ourCategory);
        catMap.put("Active", true);
        catMap.put("CategoryId", "109");
        catMap.put("Comment", "delete me so hard!");
        cmd.addRow(catMap);
        SaveRowsResponse response = cmd.execute(createDefaultConnection(false), getProjectName());

        PackageListPage listPage = PackageListPage.beginAt(this , getProjectName());
        EditCategoriesPage catPage = listPage.clickEditCategories();

        // after clicking 'save' we expect to have to dismiss the dialog for deleting the 'weight' category

        catPage.deleteCategory(ourCategory);
        catPage.clickSave();
        ModalDialog.finder(getDriver())
                .withBodyTextContaining("Are you sure you want to delete row").find()
                .dismiss("Submit Changes");
        waitFor(()-> false, 2000);

        SelectRowsCommand catsCmd = new SelectRowsCommand("snd", "PkgCategories");
        SelectRowsResponse afterCats = catsCmd.execute(createDefaultConnection(false), getProjectName());

        assertFalse("our category should have been deleted, but was not",
                afterCats.getRows().stream().anyMatch((a)-> a.get("Description").equals(ourCategory)));
    }


    @Test
    public void editCategories() throws Exception
    {
        String ourCategory = "a category to be edited for test purposes";
        String editedCategory = "a category that has been edited for test purposes";
        InsertRowsCommand cmd = new InsertRowsCommand("snd", "PkgCategories");
        Map<String, Object> catMap = new HashMap<>();
        catMap.put("Description", ourCategory);
        catMap.put("Active", true);
        catMap.put("CategoryId", "108");
        catMap.put("Comment", "edit me so hard!");
        cmd.addRow(catMap);
        SaveRowsResponse response = cmd.execute(createDefaultConnection(false), getProjectName());

        PackageListPage listPage = PackageListPage.beginAt(this , getProjectName());

        EditCategoriesPage catPage = listPage.clickEditCategories();

        // edit an existing one
        CategoryEditRow editRow = CategoryEditRow.finder(getDriver()).withDescription(ourCategory).timeout(5000).find();
        editRow
                .setActive(false)
                .setDescription(editedCategory);
        catPage = catPage.clickSave();
        sleep(2000);

        CategoryEditRow surgeryRowCat = catPage.getCategory("Surgery");
        assertNotNull("Surgery category should exist", surgeryRowCat);
        assertEquals("Surgery category should be active", true, surgeryRowCat.getIsActive());

        CategoryEditRow ourCat = catPage.getCategory(editedCategory);
        assertNotNull("test edit category should exist", ourCat);
        assertEquals("test edit category should be inactive", false, ourCat.getIsActive());
        assertEquals("test edit category should have new description", editedCategory, ourCat.getDescription());

        SelectRowsCommand catsCmd = new SelectRowsCommand("snd", "PkgCategories");
        SelectRowsResponse afterCats = catsCmd.execute(createDefaultConnection(false), getProjectName());
        assertFalse("our category should have been edited, but was not",
                afterCats.getRows().stream().anyMatch((a)-> a.get("Description").equals(editedCategory)));
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

    public void testPackageApis()
    {   DataRegionTable dataRegionTable;

        goToProjectHome();
        goToSchemaBrowser();
        viewQueryData("snd", "PkgCategories");
        assertTextPresent("Surgery", "Blood Draw", "Weight", "Vitals");

        //insert package
        runScript(SAVEPACKAGEAPI);
        goToSchemaBrowser();
        viewQueryData("snd", "Pkgs");
        assertTextPresent("My package description", 1);

        List<Map<String, Object>> packages = executeSelectRowCommand("snd", "Pkgs").getRows();
        String newPackageId = packages.stream()
                .filter(a->a.get("Description").equals("My package description"))
                .findAny().get()
                .get("PkgId").toString();

        //get package json
        String result = (String) executeAsyncScript(getPackageWithId(newPackageId));
        JSONObject resultAsJson = new JSONObject(result);
        assertEquals("Wrong narrative","This is a narrative", resultAsJson.getString("narrative"));

        JSONArray attributes = resultAsJson.getJSONArray("attributes");
        assertEquals("Wrong attribute count",3,attributes.length());

        JSONArray categories = resultAsJson.getJSONArray("categories");
        assertEquals("Wrong category count",2,categories.length());
        assertEquals("Wrong categories", Arrays.asList(102, 103), Arrays.asList(categories.toArray()));


        JSONArray validators = attributes.getJSONObject(0).getJSONArray("validators");
        assertEquals("Wrong validator count",1,validators.length());

        //confirm package currently has no event
        goToSchemaBrowser();
        dataRegionTable = viewQueryData("snd", "Pkgs");
        assertEquals("Has event not false","false",dataRegionTable.getDataAsText(0,"Has Event"));
        assertEquals("Has project not false","false",dataRegionTable.getDataAsText(0,"Has Project"));

        //create event
        runScript(ADDEVENT);
        refresh();
        dataRegionTable = new DataRegionTable("query",this);
        int rowIndex = dataRegionTable.getRowIndex("Description", "My package description");
        assertEquals("Has event not true","true",dataRegionTable.getDataAsText(rowIndex,"Has Event"));
        assertEquals("Has project not false","false",dataRegionTable.getDataAsText(rowIndex,"Has Project"));

        //add package to project
        runScript(ADDPACKAGETOPROJECT);
        refresh();
        dataRegionTable = new DataRegionTable("query",this);
        rowIndex = dataRegionTable.getRowIndex("Description", "My package description");
        assertEquals("Has event not true","true",dataRegionTable.getDataAsText(rowIndex,"Has Event"));
        assertEquals("Has project not true","true",dataRegionTable.getDataAsText(rowIndex,"Has Project"));
    }

    @Test
    public void testSNDImport() throws Exception
    {
        //go to SND Project
        clickProject(PROJECTNAME);

        //set pipeline root
        setPipelineRoot(INITIAL_IMPORT_FILE.getParentFile().getAbsolutePath());

        //go to Pipeline module
        goToModule("Pipeline");

        //import 1_initial.snd.xml
        clickButton("Process and Import Data", defaultWaitForPage);
        _fileBrowserHelper.checkFileBrowserFileCheckbox(INITIAL_IMPORT_FILE.getName());
        _fileBrowserHelper.selectImportDataAction("SND document import");
        waitForPipelineJobsToComplete(EXPECTED_IMPORT_JOBS, "SND Import ("+INITIAL_IMPORT_FILE+")", false, IMPORT_WAIT_TIME);

        //go to grid view for snd.pkg
        goToSchemaBrowser();
        selectQuery("snd", "Pkgs");
        waitAndClickAndWait(Locator.linkWithText("view data"));

        DataRegionTable results = new DataRegionTable("query", getDriver());
        assertTrue("Expect the package we registered in this test to be there",
                results.getColumnDataAsText("Description").contains("Vitals"));

        List<List<String>> rows = results.getRows("PkgId", "Description", "Active", "Repeatable", "Narrative");
        List<String> row_expected = Arrays.asList("2", "Vitals", "true", "true", "Check Vitals Test");
        assertEquals("Initial package insert - data not as expected in snd.Pkgs", row_expected, rows.get(0));

        //TODO: Uncomment below and test for validity - I was unable to test since these tables are not exposed yet
//        //DomainDescriptor
//        Connection conn = new Connection(WebTestHelper.getBaseURL(), PasswordUtil.getUsername(), PasswordUtil.getPassword());
//        SelectRowsCommand selectFromDomainDescriptor = new SelectRowsCommand("exp", "DomainDescriptor");
//        selectFromDomainDescriptor.addFilter(new Filter("Name", "Package-1", Filter.Operator.EQUAL));
//        SelectRowsResponse resp = selectFromDomainDescriptor.execute(conn, getContainerPath());
//        assertEquals("exp.DomainDescriptor does not contain a row where Name equals Package-1", 1, resp.getRows().size());
//
//        Map<String, Object> domainDescriptorRow = resp.getRows().get(0);
//        int domainId = (int) domainDescriptorRow.get("DomainId");
//
//        //PropertyDomain
//        SelectRowsCommand selectFromPropertyDomain = new SelectRowsCommand("exp", "PropertyDomain");
//        selectFromPropertyDomain.addFilter(new Filter("DomainId", domainId, Filter.Operator.EQUAL));
//        resp = selectFromPropertyDomain.execute(conn, getContainerPath());
//        assertEquals("exp.PropertyDomain does not contain a row with expected DomainId " + domainId, 1, resp.getRows().size());
//
//        Map<String, Object> propertyDomain= resp.getRows().get(0);
//        int propertyId = (int) propertyDomain.get("PropertyId");
//
//        //PropertyDescriptor
//        SelectRowsCommand selectFromPropertyDescriptor = new SelectRowsCommand("exp", "PropertyDescriptor");
//        selectFromPropertyDescriptor.addFilter(new Filter("PropertyId", propertyId, Filter.Operator.EQUAL));
//        resp = selectFromPropertyDescriptor.execute(conn, getContainerPath());
//        assertEquals("exp.PropertyDescriptor does not contain a row with expected Propertyid " + propertyId, 1, resp.getRows().size());

        //import 2_changeNarrative.snd.xml
        goToModule("Pipeline");
        clickButton("Process and Import Data", defaultWaitForPage);
        _fileBrowserHelper.checkFileBrowserFileCheckbox(CHANGE_NARRATIVE_FILE.getName());
        _fileBrowserHelper.selectImportDataAction("SND document import");
        waitForPipelineJobsToComplete(++EXPECTED_IMPORT_JOBS, "SND Import ("+CHANGE_NARRATIVE_FILE+")", false, IMPORT_WAIT_TIME);

        //go to grid view
        goToSchemaBrowser();
        selectQuery("snd", "Pkgs");
        waitAndClickAndWait(Locator.linkWithText("view data"));

        results = new DataRegionTable("query", getDriver());
        rows = results.getRows("PkgId", "Description", "Active", "Repeatable", "Narrative");
        row_expected = Arrays.asList("2", "Vitals", "true", "true", "Check Vitals");
        assertEquals("Updated narrative - data not as expected in snd.Pkgs", row_expected, rows.get(0));

        //import 3_insertPackage.snd.xml
        goToModule("Pipeline");
        clickButton("Process and Import Data", defaultWaitForPage);
        _fileBrowserHelper.checkFileBrowserFileCheckbox(INSERT_PACKAGE_FILE.getName());
        _fileBrowserHelper.selectImportDataAction("SND document import");
        waitForPipelineJobsToComplete(++EXPECTED_IMPORT_JOBS, "SND Import ("+INSERT_PACKAGE_FILE+")", false, IMPORT_WAIT_TIME);

        // go to grid view
        goToSchemaBrowser();
        selectQuery("snd", "Pkgs");
        waitAndClickAndWait(Locator.linkWithText("view data"));

        results = new DataRegionTable("query", getDriver());
        assertEquals("Wrong row count in snd.Pkgs",2, results.getDataRowCount());

        rows = results.getRows("PkgId", "Description", "Active", "Repeatable", "Narrative");
        row_expected = Arrays.asList("1", "Therapy", "false", "true", "Therapy started");
        assertEquals("New Package inserted - data not as expected in snd.Pkgs", row_expected, rows.get(0));

        //import 4_addAttribute.snd.xml
        goToModule("Pipeline");
        clickButton("Process and Import Data", defaultWaitForPage);
        _fileBrowserHelper.checkFileBrowserFileCheckbox(ADD_ATTRIBUTE_FILE.getName());
        _fileBrowserHelper.selectImportDataAction("SND document import");
        waitForPipelineJobsToComplete(++EXPECTED_IMPORT_JOBS, "SND Import ("+ADD_ATTRIBUTE_FILE+")", false, IMPORT_WAIT_TIME);

        //go to grid view
        //TODO: Test attribute addition in exp tables (not exposed yet)

        //import 5_removeAttribute.snd.xml
        goToModule("Pipeline");
        clickButton("Process and Import Data", defaultWaitForPage);
        _fileBrowserHelper.checkFileBrowserFileCheckbox(REMOVE_ATTRIBUTE_FILE.getName());
        _fileBrowserHelper.selectImportDataAction("SND document import");
        waitForPipelineJobsToComplete(++EXPECTED_IMPORT_JOBS, "SND Import ("+REMOVE_ATTRIBUTE_FILE+")", false, IMPORT_WAIT_TIME);

        //go to grid view
        //TODO: Test attribute removal in exp tables (not exposed yet)

        //import 6_removeAllAttributes.snd.xml
        goToModule("Pipeline");
        clickButton("Process and Import Data", defaultWaitForPage);
        _fileBrowserHelper.checkFileBrowserFileCheckbox(REMOVE_ALL_ATTRIBUTES_FILE.getName());
        _fileBrowserHelper.selectImportDataAction("SND document import");
        waitForPipelineJobsToComplete(++EXPECTED_IMPORT_JOBS, "SND Import ("+REMOVE_ALL_ATTRIBUTES_FILE+")", true, IMPORT_WAIT_TIME);

        checkExpectedErrors(1);
    }

    private void truncateSndPkg() throws Exception
    {
        //cleanup - truncate snd.pkgs
        Connection conn = createDefaultConnection(false);
        TruncateTableCommand command = new TruncateTableCommand("snd", "Pkgs");
        command.execute(conn, getProjectName());

        conn = createDefaultConnection(false);
        SelectRowsCommand selectRowsCommand = new SelectRowsCommand("snd", "Pkgs");
        SelectRowsResponse selectRowsResponse = selectRowsCommand.execute(conn, getProjectName());
        assertEquals("Zero row count expected after truncating snd.Pkgs", 0, selectRowsResponse.getRows().size());
    }
}