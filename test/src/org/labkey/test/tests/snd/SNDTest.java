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
import org.labkey.test.pages.snd.BeginPage;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.Maps;
import org.labkey.test.util.PasswordUtil;
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
    private static final String PROJECTNAME = "SNDTest Project";
    private static final String TEST1SUBFOLDER = "Test1";
    private static final String TEST1PATH = PROJECTNAME + "/" + TEST1SUBFOLDER;
    private static final String PKGSTESTCOL = "testPkgs";
    private static final String EXTCOLTESTDATA1 = "testString 1";
    private static final String EXTCOLTESTDATA2 = "testString 2";
    private static final String EXTCOLTESTDATA3 = "testString 3";
    private static final String EXTCOLTESTDATA3A = "updated testString 3";

    private static final String CREATEDOMAINSAPI = "LABKEY.Domain.create({\n" +
            "   failure: function (e) {\n" +
            "        LABKEY.Utils.alert(\"Error\", e.exception);\n" +
            "    },\n" +
            "   domainGroup: \"test\",\n" +
            "   domainKind: \"SND\",\n" +
            "   module: \"snd\",\n" +
            "   importData: false\n" +
            "});\n";

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
        clickFolder(getProjectName());
    }

    private void setupTest1Project()
    {
        clickFolder(TEST1SUBFOLDER);
        addTestColumns();
    }

    private void addTestColumns()
    {
        executeScript(CREATEDOMAINSAPI);
    }

    @Before
    public void preTest()
    {
        goToProjectHome();
    }

    @Test
    public void testSNDModule()
    {
        _containerHelper.enableModule("SND");
        BeginPage beginPage = BeginPage.beginAt(this, getProjectName());
        assertEquals(200, getResponseCode());
        final String expectedHello = "Hello, and welcome to the SND module.";
        assertEquals("Wrong hello message", expectedHello, beginPage.getHelloMessage());
    }

    @Override
    protected BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Override
    protected String getProjectName()
    {
        return "SNDTest Project";
    }

    @Override
    public List<String> getAssociatedModules()
    {
        return Collections.singletonList("SND");
    }

    @Test
    public void testExtensibleColumns() throws IOException, CommandException
    {
        clickFolder(TEST1SUBFOLDER);

        Connection cn = new Connection(getBaseURL(), PasswordUtil.getUsername(), PasswordUtil.getPassword());

        InsertRowsCommand insertRowsCommand = new InsertRowsCommand("snd", "Pkgs");
        insertRowsCommand.addRow(TEST1ROW1MAP);
        insertRowsCommand.addRow(TEST1ROW2MAP);
        insertRowsCommand.addRow(TEST1ROW3MAP);
        SaveRowsResponse resp = insertRowsCommand.execute(cn, getProjectName() + "/" + TEST1SUBFOLDER);
        assert resp.getRowsAffected().intValue() == 3;

        goToSchemaBrowser();
        selectQuery("snd", "Pkgs");
        assertTextPresent(PKGSTESTCOL);
        waitAndClickAndWait(Locator.linkWithText("view data"));

        assertTextPresent(EXTCOLTESTDATA1, EXTCOLTESTDATA2, EXTCOLTESTDATA3);

        clickFolder(TEST1SUBFOLDER);

        UpdateRowsCommand updateRowsCommand = new UpdateRowsCommand("snd", "Pkgs");
        updateRowsCommand.addRow(TEST1ROW3AMAP);
        resp = updateRowsCommand.execute(cn, getProjectName() + "/" + TEST1SUBFOLDER);
        assert resp.getRowsAffected().intValue() == 1;

        goToSchemaBrowser();
        selectQuery("snd", "Pkgs");
        waitAndClickAndWait(Locator.linkWithText("view data"));
        assertTextPresent(EXTCOLTESTDATA1, EXTCOLTESTDATA2, EXTCOLTESTDATA3A, "Updated Description 3");

        clickFolder(TEST1SUBFOLDER);

        DeleteRowsCommand deleteRowsCommand = new DeleteRowsCommand("snd", "Pkgs");
        deleteRowsCommand.addRow(TEST1ROW2MAP);
        resp = deleteRowsCommand.execute(cn, getProjectName() + "/" + TEST1SUBFOLDER);
        assert resp.getRowsAffected().intValue() == 1;

        goToSchemaBrowser();
        selectQuery("snd", "Pkgs");
        waitAndClickAndWait(Locator.linkWithText("view data"));
        assertTextNotPresent(EXTCOLTESTDATA2, "Description 2");
    }
}