package org.labkey.test.pages.snd;

import org.apache.commons.lang3.NotImplementedException;
import org.labkey.test.Locator;
import org.labkey.test.WebDriverWrapper;
import org.labkey.test.WebTestHelper;
import org.labkey.test.components.snd.CategoryEditRow;
import org.labkey.test.pages.LabKeyPage;
import org.labkey.test.selenium.LazyWebElement;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

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
        CategoryEditRow newCategory = CategoryEditRow.finder(getDriver()).withEmptyDescription().find();
        newCategory
                .setDescription(category)
                .setActive(active);
        return this;
    }

    public CategoryEditRow getCategory(String category)
    {
        return CategoryEditRow.finder(getDriver()).withDescription(category).timeout(3000).findOrNull();
    }

    public EditCategoriesPage deleteCategory(String category)
    {
        CategoryEditRow toDelete = CategoryEditRow.finder(getDriver())
                .withDescription(category).timeout(4000).find();
        toDelete.delete();
        return this;
    }

    public List<CategoryEditRow> getAllCategories()
    {
        return CategoryEditRow.finder(getDriver()).findAll();
    }

    public EditCategoriesPage clickSave()
    {
        waitFor(()-> elementCache().saveButton.getAttribute("disabled")==null,
                "'Save' button is disabled", 2000);
        elementCache().saveButton.click();
        sleep(1000);    // todo: wait for save button to detatch/unmount before re-mounting into next page view
        return new EditCategoriesPage(getDriver());
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
        WebElement addCategoriesBtn = Locator.tag("div").containing("Add Category")
                .withChild(Locator.tagWithClass("i", "fa fa-plus-circle"))
                .findWhenNeeded(getDriver()).withTimeout(4000);
        WebElement cancelButton = Locator.button("Cancel").findWhenNeeded(getDriver()).withTimeout(4000);
        WebElement saveButton = Locator.button("Save").findWhenNeeded(getDriver()).withTimeout(4000);
    }
}