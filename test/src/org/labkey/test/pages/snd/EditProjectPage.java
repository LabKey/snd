package org.labkey.test.pages.snd;

import org.labkey.test.Locator;
import org.labkey.test.WebDriverWrapper;
import org.labkey.test.WebTestHelper;
import org.labkey.test.components.html.Checkbox;
import org.labkey.test.components.snd.AttributesGrid;
import org.labkey.test.components.snd.SuperPackageRow;
import org.labkey.test.pages.LabKeyPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

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

    private void setDate(WebElement input, String date)
    {
        if (!isHtml5InputTypeSupported("date"))
        {
            throw new RuntimeException("SND requires HTML5 date inputs. Please rerun test in Chrome");
        }
        setFormElement(input, date);
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
        setDate(elementCache().startDateTextBox, date);
        return this;
    }

    public String getStartDate()
    {
        return elementCache().startDateTextBox.getAttribute("value");
    }

    public EditProjectPage setEndDate(String date)
    {
        setDate(elementCache().endDateTextBox, date);
        return this;
    }

    public String getEndDate()
    {
        return elementCache().endDateTextBox.getAttribute("value");
    }

    public EditProjectPage setEndDateRevised(String date)
    {
        setFormElement(elementCache().endDateRevisedTextBox,date);
        return this;
    }

    public String getEndDateRevised()
    {
        return elementCache().endDateRevisedTextBox.getAttribute("value");
    }

    public EditProjectPage setPrimitivesOnlyCheckBox(boolean set)
    {
        new Checkbox(elementCache().primitivesOnlyCheckBox).set(set);
        return this;
    }

    public EditProjectPage setRevisionCopyPkgsCheckBox(boolean set)
    {
        new Checkbox(elementCache().copyRevisedPkgsChkBox).set(set);
        return this;
    }

    public List<SuperPackageRow> getAvailablePackages()
    {
        return SuperPackageRow.finder(getDriver()).timeout(4000)
                .findAll(elementCache().querySearchContainer);
    }

    public SuperPackageRow getAvailablePackage(String partialDescription)
    {
        return SuperPackageRow.finder(getDriver()).timeout(4000)
                .withPartialDescription(partialDescription).find(elementCache().querySearchContainer);
    }

    public List<SuperPackageRow> getAssignedPackages()
    {
        return SuperPackageRow.finder(getDriver())
                .findAll(elementCache().assignedPackageContainer);
    }

    public boolean isAssignedPackagePresent(String partialDescription)
    {
        return null != SuperPackageRow.finder(getDriver()).timeout(4000)
                .withPartialDescription(partialDescription).findOrNull(elementCache().assignedPackageContainer);
    }

    public SuperPackageRow getAssignedPackage(String partialDescription)
    {
        return SuperPackageRow.finder(getDriver()).timeout(4000)
                .withPartialDescription(partialDescription).find(elementCache().assignedPackageContainer);
    }

    public String getAssignedPackageText(String packageDescription)
    {
        getAssignedPackage(packageDescription).select();
        waitFor(()-> Locator.tagWithClass("div", "data-search__row")
                .findElementOrNull(elementCache().selectedPackageNarrativeViewPane) != null, 2000);

        return Locator.tagWithClass("div", "data-search__row")
                .findElement(elementCache().selectedPackageNarrativeViewPane).getText();
    }

    public ProjectListPage clickProjectsCrumb()
    {
        elementCache().projectListCrumb.click();
        ProjectListPage plp = new ProjectListPage(getDriver());
        plp.waitForPageLoad();
        return plp;
    }

    public ProjectListPage clickSave()
    {
        waitFor(()-> elementCache().saveButton.getAttribute("disabled")==null,
                "'Save' button is disabled", 2000);
        elementCache().saveButton.click();
        ProjectListPage plp = new ProjectListPage(getDriver());
        plp.waitForPageLoad();
        return plp;
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
        ProjectListPage plp = new ProjectListPage(getDriver());
        plp.waitForPageLoad();
        return plp;
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

        WebElement projectListCrumb = Locator.tag("a")
                .withAttribute("href","#/projects")
                .findWhenNeeded(getDriver())
                .withTimeout(4000);

        WebElement appContainer = Locator.tagWithId("div", "app").findWhenNeeded(this);
        WebElement panelBody = Locator.tagWithClass("div", "panel-body").findWhenNeeded(appContainer);

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

        WebElement endDateTextBox = Locator.tagWithClass("input","details-input")
                .withAttribute("name","endDate")
                .findWhenNeeded(getDriver())
                .withTimeout(4000);

        WebElement endDateRevisedTextBox = Locator.tagWithClass("input","details-input")
                .withAttribute("name","endDateRevised")
                .findWhenNeeded(getDriver())
                .withTimeout(4000);

        WebElement copyRevisedPkgsChkBox = Locator.tag("input")
                .withAttribute("name", "copyRevisedPkgs")
                .findWhenNeeded(getDriver())
                .withTimeout(4000);

        WebElement descriptionEdit = Locator.tagWithClass("textarea", "form-control")
                .withAttribute("name", "description")
                .findWhenNeeded(getDriver())
                .withTimeout(4000);

        WebElement primitivesOnlyCheckBox = Locator.tagWithAttribute("input", "type", "checkbox")
                .findWhenNeeded(querySearchContainer).withTimeout(4000);

        WebElement assignedPackageContainer = Locator.tagWithClassContaining("div", "row")
                .withChild(Locator.tagWithClass("label", "control-label").withText("Assigned Packages"))
                .findWhenNeeded(getDriver()).withTimeout(4000);
        WebElement selectedPackageNarrativeViewPane = Locator.tagWithClassContaining("div", "row")
                .withChild(Locator.tagWithClass("label", "control-label").withText("Assigned Packages")).followingSibling("div")
                .findWhenNeeded(getDriver()).withTimeout(4000);

        WebElement cancelButton = Locator.tagWithId("button", "cancelButton").findWhenNeeded(getDriver());
        WebElement saveButton = Locator.tagWithId("button", "save").findWhenNeeded(getDriver());
        WebElement saveAsDraftButton = Locator.tagWithId("button", "saveAsDraft").findWhenNeeded(getDriver());

    }
}









