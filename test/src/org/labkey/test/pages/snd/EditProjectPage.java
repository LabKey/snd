package org.labkey.test.pages.snd;

import org.labkey.test.Locator;
import org.labkey.test.WebDriverWrapper;
import org.labkey.test.WebTestHelper;
import org.labkey.test.components.html.Checkbox;
import org.labkey.test.components.snd.AttributesGrid;
import org.labkey.test.pages.LabKeyPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class EditProjectPage extends LabKeyPage<EditProjectPage.ElementCache>
{
    public EditProjectPage(WebDriver driver)
    {
        super(driver);
    }

    public static EditProjectPage beginAt(WebDriverWrapper driver)
    {
        return beginAt(driver, driver.getCurrentContainerPath());
    }

    public static EditProjectPage beginAt(WebDriverWrapper driver, String containerPath)
    {
        driver.beginAt(WebTestHelper.buildURL("controller", containerPath, "action"));
        return new EditProjectPage(driver.getDriver());
    }

    @Override
    protected void waitForPage()
    {
        waitFor(() -> {
            try
            {
                return elementCache().grid.getComponentElement().isDisplayed();
            }
            catch (NoSuchElementException retry)
            {
                return false;
            }
        }, WAIT_FOR_JAVASCRIPT);
    }

    public EditProjectPage setDescription(String description)
    {
        setFormElement(elementCache().descriptionEdit, description);
        return this;
    }

    public String getDescription()
    {
        return elementCache().descriptionEdit.getAttribute("value");
    }

    public EditProjectPage setReferenceId(String refId)
    {
        setFormElement(elementCache().referenceIdTextBox,refId);
        return this;
    }

    public String getReferenceId()
    {
        return elementCache().referenceIdTextBox.getAttribute("value");
    }

    public EditProjectPage setStartDate(String date)
    {
        setFormElement(elementCache().startDateTextBox,date);
        return this;
    }


    public String getStartDate()
    {
        return elementCache().startDateTextBox.getAttribute("value");
    }


    public EditProjectPage setPrimitivesOnlyCheckBox(boolean set)
    {
        new Checkbox(elementCache().primitivesOnlyCheckBox).set(set);
        return this;
    }

    public ProjectListPage clickSave()
    {
        waitFor(()-> elementCache().saveButton.getAttribute("disabled")==null,
                "'Save' button is disabled", 2000);
        elementCache().saveButton.click();
        shortWait().until(ExpectedConditions.stalenessOf(elementCache().saveButton));
        return new ProjectListPage(getDriver());
    }

    public ProjectListPage clickCancel()
    {
        elementCache().cancelButton.click();
        return new ProjectListPage(getDriver());
    }

    public ProjectListPage clickSaveAsDraft()
    {
        waitFor(()-> elementCache().saveAsDraftButton.getAttribute("disabled")==null,
                "'Save as Draft' button is disabled", 2000);
        elementCache().saveAsDraftButton.click();
        shortWait().until(ExpectedConditions.stalenessOf(elementCache().saveAsDraftButton));
        return new ProjectListPage(getDriver());
    }

    protected ElementCache newElementCache()
    {
        return new ElementCache();
    }
    protected class ElementCache extends LabKeyPage.ElementCache
    {
        WebElement querySearchContainer = Locator.tagWithClass("div", "query-search--container")
                .withDescendant(Locator.tagWithClassContaining("span", "data-search__container"))
                .findWhenNeeded(getDriver()).withTimeout(4000);

        WebElement appContainer = Locator.tagWithId("div", "app").findWhenNeeded(this);
        WebElement panelBody = Locator.tagWithClass("div", "panel-body").findWhenNeeded(appContainer);

        WebElement backToProjectsBtn = Locator.tagWithClass("i", "fa fa-arrow-circle-left").parent() // parent is <a href="#/packages"
                .withChild(Locator.tagWithText("h4", "Projects")).findWhenNeeded(getDriver());
        WebElement projectAttributesPanel = Locator.tagWithClass("div", "row")
                .withDescendant(Locator.tagContainingText("strong", "Attributes"))
                .findWhenNeeded(panelBody);

        AttributesGrid grid = new AttributesGrid(
                Locator.tagWithClassContaining("div", "margin")
                        .withChild(Locator.tagWithText("strong", "Attributes"))
                        .followingSibling("div").withDescendant(Locator.tag("table"))
                        .refindWhenNeeded(getDriver()).withTimeout(4000),
                getDriver());

        WebElement referenceIdTextBox = Locator.tagWithClass("input","form-control")
                .withAttribute("name","referenceId")
                .findWhenNeeded(getDriver())
                .withTimeout(4000);

        WebElement startDateTextBox = Locator.tagWithClass("input","details-input")
                .withAttribute("name","startDate")
                .findWhenNeeded(getDriver())
                .withTimeout(4000);

        WebElement descriptionEdit = Locator.tagWithClass("textarea", "form-control")
                .withAttribute("name", "description")
                .findWhenNeeded(getDriver())
                .withTimeout(4000);

        WebElement primitivesOnlyCheckBox = Locator.tagWithAttribute("input", "type", "checkbox")
                .findWhenNeeded(querySearchContainer).withTimeout(4000);



        WebElement cancelButton = Locator.tagWithId("button", "cancelButton").findWhenNeeded(getDriver());
        WebElement saveButton = Locator.tagWithId("button", "save").findWhenNeeded(getDriver());
        WebElement saveAsDraftButton = Locator.tagWithId("button", "saveAsDraft").findWhenNeeded(getDriver());

    }
}









