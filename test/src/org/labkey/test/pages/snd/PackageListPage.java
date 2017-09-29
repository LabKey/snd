package org.labkey.test.pages.snd;

import org.labkey.test.Locator;
import org.labkey.test.WebDriverWrapper;
import org.labkey.test.WebTestHelper;
import org.labkey.test.components.html.BootstrapMenu;
import org.labkey.test.components.html.Input;
import org.labkey.test.components.snd.PackageViewerResult;
import org.labkey.test.pages.LabKeyPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PackageListPage extends LabKeyPage<PackageListPage.ElementCache>
{
    public PackageListPage(WebDriver driver)
    {
        super(driver);
    }

    public static PackageListPage beginAt(WebDriverWrapper driver)
    {
        return beginAt(driver, driver.getCurrentContainerPath());
    }

    public static PackageListPage beginAt(WebDriverWrapper driver, String containerPath)
    {
        driver.beginAt(WebTestHelper.buildURL("snd", containerPath, "app") + "#/packages");
        return new PackageListPage(driver.getDriver());
    }

    public EditPackagePage clickNewPackage()
    {
        elementCache().newPackageButton.click();

        return new EditPackagePage(getDriver());
    }

    public PackageListPage showDrafts(boolean set)
    {
        if (set)
            checkCheckbox(elementCache().showDraftsToggle);
        else
            uncheckCheckbox(elementCache().showDraftsToggle);
        return this;
    }

    public BootstrapMenu getOptionsMenu()
    {
        return BootstrapMenu.finder(getDriver()).withButtonText("Options").findWhenNeeded(elementCache().searchHeader);
    }

    public EditCategoriesPage clickEditCategories()
    {
        getOptionsMenu().clickSubMenu(false, "Edit Categories");
        return new EditCategoriesPage(getDriver());
    }

    public PackageListPage setSearchFilter(String searchExpression)
    {
        elementCache().searchFilter.set(searchExpression);
        return this;
    }

    public PackageViewerResult getPackage(String partialText)
    {
        return PackageViewerResult.finder(getDriver()).containingText(partialText)
                .timeout(2000).findWhenNeeded(elementCache().container);
    }

    protected ElementCache newElementCache()
    {
        return new ElementCache();
    }

    protected class ElementCache extends LabKeyPage.ElementCache
    {

        WebElement container = Locator.tagWithClass("div", "query-search--container")
                .refindWhenNeeded(this).withTimeout(WAIT_FOR_JAVASCRIPT);
        WebElement searchHeader = Locator.tagWithClass("div", "package-viewer__header")
                .refindWhenNeeded(container);
        WebElement newPackageButton = Locator.button("New Package").findWhenNeeded(searchHeader).withTimeout(2000);
        WebElement showDraftsToggle = Locator.tagWithClassContaining("div", "PackageSearch__packages-show_drafts")
                .withText("Show drafts")
                .child("input")
                .findWhenNeeded(searchHeader).withTimeout(4000);
        Input searchFilter = Input.Input(Locator.input("packageSearch"), getDriver()).timeout(4000)
            .findWhenNeeded(container);
    }
}