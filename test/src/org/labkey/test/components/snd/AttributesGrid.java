package org.labkey.test.components.snd;

import org.labkey.test.Locator;
import org.labkey.test.components.WebDriverComponent;
import org.labkey.test.components.html.Input;
import org.labkey.test.pages.LabKeyPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.labkey.test.components.html.Input.Input;

public class AttributesGrid extends WebDriverComponent<AttributesGrid.ElementCache>
{
    final WebElement _el;
    final WebDriver _driver;

    public AttributesGrid(WebElement element, WebDriver driver)
    {
        _el = element;
        _driver = driver;
    }

    @Override
    public WebElement getComponentElement()
    {
        return _el;
    }

    @Override
    public WebDriver getDriver()
    {
        return _driver;
    }

    public AttributesGrid waitForRows(Integer expectedRowCount)
    {
        getWrapper().waitFor(()->  getRows().size() == expectedRowCount,
                "expected number of rows [" + expectedRowCount.toString() + "] did not appear in time",
                4000);
        return this;
    }


    public List<WebElement> getHeaderCells()
    {
        return elementCache().getHeaderCells();
    }

    public List<WebElement> getRows()
    {
        return elementCache().getRows();
    }

    public AttributeGridRow getRow(String key)
    {
        return elementCache().getRow(key);
    }

    protected ElementCache newElementCache()
    {
        return new ElementCache();
    }

    protected class ElementCache extends WebDriverComponent.ElementCache
    {
        public List<WebElement> getRows()
        {
            return Locators.rows.findElements(getComponentElement());
        }

        public AttributeGridRow getRow(String key)
        {
            return AttributeGridRow.finder(AttributesGrid.this).withKey(key)
                    .timeout(4000).find(getComponentElement());
        }

        public List<WebElement> getHeaderCells()
        {
            return Locators.headerCells.findElements(getComponentElement());
        }
    }

    public static class Locators
    {
        public static final Locator rows = Locator.css("tbody tr");
        public static final Locator headerCells = Locator.xpath("//thead/tr/th/strong");
    }
}