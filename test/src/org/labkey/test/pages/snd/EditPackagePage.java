package org.labkey.test.pages.snd;

import org.labkey.test.Locator;
import org.labkey.test.WebDriverWrapper;
import org.labkey.test.WebTestHelper;
import org.labkey.test.components.snd.AttributesGrid;
import org.labkey.test.components.snd.FilterSelect;
import org.labkey.test.pages.LabKeyPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;

public class EditPackagePage extends LabKeyPage<EditPackagePage.ElementCache>
{
    public EditPackagePage(WebDriver driver)
    {
        super(driver);
    }

    public static EditPackagePage beginAt(WebDriverWrapper driver)
    {
        return beginAt(driver, driver.getCurrentContainerPath());
    }

    public static EditPackagePage beginAt(WebDriverWrapper driver, String containerPath)
    {
        driver.beginAt(WebTestHelper.buildURL("controller", containerPath, "action"));
        return new EditPackagePage(driver.getDriver());
    }

    @Override
    protected void waitForPage()
    {
        waitFor(()-> {
            try{
                return elementCache().grid.getComponentElement().isDisplayed();
            } catch (NoSuchElementException retry){return false;}
        }, WAIT_FOR_JAVASCRIPT);
    }

    public PackageListPage clickBackToPackagesButton()
    {
        elementCache().backToPackagesBtn.click();

        return new PackageListPage(getDriver());
    }

    public EditPackagePage setDescription(String description)
    {
        setFormElement(elementCache().descriptionEdit, description);
        return this;
    }

    public String getDescription()
    {
        return elementCache().descriptionEdit.getAttribute("value");
    }

    public EditPackagePage setNarrative(String description)
    {
        setFormElement(elementCache().narrativeTextArea, description);
        return this;
    }

    public String getNarrative()
    {
        return elementCache().narrativeTextArea.getText();
    }

    public AttributesGrid getAttributesGrid()
    {
        return elementCache().grid;
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
        elementCache().cancelButton.click();
        return new PackageListPage(getDriver());
    }

    public PackageListPage clickSaveAsDraft()
    {
        waitFor(()-> elementCache().saveAsDraftButton.getAttribute("disabled")==null,
                "'Save as Draft' button is disabled", 2000);
        elementCache().saveAsDraftButton.click();
        return new PackageListPage(getDriver());
    }

    public PackageListPage clickSubmitForReview()
    {
        waitFor(()-> elementCache().submitForReveiwButton.getAttribute("disabled")==null,
                "'Submit for Review' button is disabled", 2000);
        elementCache().submitForReveiwButton.click();
        return new PackageListPage(getDriver());
    }

    protected ElementCache newElementCache()
    {
        return new ElementCache();
    }

    protected class ElementCache extends LabKeyPage.ElementCache
    {
        WebElement appContainer = Locator.tagWithId("div", "app").findWhenNeeded(this);
        WebElement backToPackagesBtn = Locator.tagWithClass("i", "fa fa-arrow-circle-left").parent() // parent is <a href="#/packages"
                .withChild(Locator.tagWithText("h4", "Packages")).findWhenNeeded(getDriver());

        WebElement panelHeading = Locator.tagWithClass("div", "panel-heading").findWhenNeeded(appContainer);
        WebElement panelBody = Locator.tagWithClass("div", "panel-body").findWhenNeeded(appContainer);

        // container for description, narrative, categories edits
        WebElement packageEditPanel = Locator.xpath("//div[@class='col-sm-8']")
                .withDescendant(Locator.tagWithClass("label", "control-label").withText("Package Id"))
                .findWhenNeeded(getDriver());
        WebElement descriptionEdit = Locator.tagWithClass("input", "form-control")
                .withAttribute("name", "description")
                .findWhenNeeded(packageEditPanel);
        WebElement narrativeTextArea = Locator.tagWithClass("textarea", "form-control")
                .findWhenNeeded(packageEditPanel);

        // container for narrative attributes grid
        WebElement packageAttributesPanel = Locator.tagWithClass("div", "row")
                .withDescendant(Locator.tagContainingText("strong", "Attributes"))
                .findWhenNeeded(panelBody);

        AttributesGrid grid = new AttributesGrid(
                Locator.tagWithClassContaining("div", "PackageForm__margin")
                        .withChild(Locator.tagWithText("strong", "Attributes"))
                        .followingSibling("div").withDescendant(Locator.tag("table"))
                        .findWhenNeeded(getDriver()),
                getDriver());

        FilterSelect categoryPicker = FilterSelect.finder(getDriver())
                .findWhenNeeded(packageEditPanel);


        // TODO: Available Packages summary (add package)
        // TODO: Assigned Packages summary  (remove package)

        WebElement cancelButton = Locator.button("Cancel").findWhenNeeded(getDriver());
        WebElement saveButton = Locator.button("Save").findWhenNeeded(getDriver());
        WebElement saveAsDraftButton = Locator.button("Save as Draft").findWhenNeeded(getDriver());
        WebElement submitForReveiwButton = Locator.button("Submit for Review").findWhenNeeded(getDriver());
    }
}