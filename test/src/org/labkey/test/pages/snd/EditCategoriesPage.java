package org.labkey.test.pages.snd;

import org.apache.commons.lang3.NotImplementedException;
import org.labkey.test.Locator;
import org.labkey.test.WebDriverWrapper;
import org.labkey.test.WebTestHelper;
import org.labkey.test.pages.LabKeyPage;
import org.labkey.test.selenium.LazyWebElement;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class EditCategoriesPage extends LabKeyPage<EditCategoriesPage.ElementCache>
{
    public EditCategoriesPage(WebDriver driver)
    {
        super(driver);
    }

    public static EditCategoriesPage beginAt(WebDriverWrapper driver)
    {
        return beginAt(driver, driver.getCurrentContainerPath());
    }

    public static EditCategoriesPage beginAt(WebDriverWrapper driver, String containerPath)
    {
        driver.beginAt(WebTestHelper.buildURL("snd", containerPath, "categories"));
        return new EditCategoriesPage(driver.getDriver());
    }

    public EditCategoriesPage addCategory(String category, boolean active)
    {
        elementCache().addCategoriesBtn.click();

        // todo: find the new category row, set the category and active state
        throw new NotImplementedException("not yet");
        //return this;
    }

    public PackageListPage clickSave()
    {
        waitFor(()-> elementCache().saveButton.getAttribute("disabled")==null,
                "'Save' button is disabled", 2000);
        elementCache().saveButton.click();
        return new PackageListPage(getDriver());
    }

    public PackageListPage clickCancel()
    {
        waitFor(()-> elementCache().cancelButton.getAttribute("disabled")==null,
                "'Cancel' button is disabled", 2000);
        elementCache().cancelButton.click();
        return new PackageListPage(getDriver());
    }

    protected ElementCache newElementCache()
    {
        return new ElementCache();
    }

    protected class ElementCache extends LabKeyPage.ElementCache
    {
        // TODO: Add other elements that are on the page
        WebElement addCategoriesBtn = Locator.tagWithClass("i", "fa fa-plus-circle")
                .parent().containing("Add Category").findWhenNeeded(getDriver());
        WebElement cancelButton = Locator.button("Cancel").findWhenNeeded(getDriver());
        WebElement saveButton = Locator.button("Save").findWhenNeeded(getDriver());
    }
}