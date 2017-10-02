package org.labkey.test.components.snd;

import org.labkey.api.cache.Cache;
import org.labkey.test.Locator;
import org.labkey.test.WebDriverWrapper;
import org.labkey.test.components.WebDriverComponent;
import org.labkey.test.components.html.Checkbox;
import org.labkey.test.components.html.Input;
import org.labkey.test.pages.LabKeyPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.labkey.test.components.html.Input.Input;

public class CategoryEditRow extends WebDriverComponent<CategoryEditRow.ElementCache>
{
    final WebElement _el;
    final WebDriver _driver;

    public CategoryEditRow(WebElement element, WebDriver driver)
    {
        _el = element;
        _driver = driver;
    }

    static public CategoryEditRowFinder finder(WebDriver driver)
    {
        return new CategoryEditRowFinder(driver);
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

    public CategoryEditRow setDescription(String value)
    {
        elementCache().descriptionInput.set(value);

        return this;
    }

    public String getDescription()
    {
        return elementCache().descriptionInput.get();
    }

    public CategoryEditRow setActive(boolean set)
    {
        elementCache().activeCheckBox.set(set);
        return this;
    }

    public boolean getIsActive()
    {
        return elementCache().activeCheckBox.get();
    }

    public void delete()
    {
        getWrapper().fireEvent(elementCache().deleteLI, WebDriverWrapper.SeleniumEvent.mouseover);
        getWrapper().waitFor(()-> elementCache().deleteLI.isEnabled() && elementCache().deleteLI.isSelected(), 2000);
        elementCache().deleteLI.click();
    }

    protected ElementCache newElementCache()
    {
        return new ElementCache();
    }

    protected class ElementCache extends WebDriverComponent.ElementCache
    {
        Input descriptionInput = Input(Locators.descriptionEdit(), getDriver()).timeout(4000).findWhenNeeded(this);
        Checkbox activeCheckBox  = Checkbox.Checkbox(
                Locator.tagWithAttributeContaining("input", "name", "[Active]")).timeout(4000)
                .findWhenNeeded(getComponentElement());
        WebElement deleteLI = Locator.xpath("//li[not(*)]")     // it's the li with no children in the row
                .findWhenNeeded(getComponentElement()).withTimeout(2000);
    }

    static public class Locators
    {

        static public Locator.XPathLocator descriptionEdit()
        {
            return Locator.tagWithAttributeContaining("input", "name","[Description]");
        }
        static public Locator.XPathLocator descriptionEdit(String desc)
        {
            return descriptionEdit().withAttribute("value", desc);
        }
    }

    public static class CategoryEditRowFinder extends WebDriverComponent.WebDriverComponentFinder<CategoryEditRow, CategoryEditRowFinder>
    {
        private Locator _locator;

        private CategoryEditRowFinder(WebDriver driver)
        {
            super(driver);
            _locator = Locators.descriptionEdit();
        }

        public CategoryEditRowFinder withDescription(String description)   // finds the row based on key column's 'title' attribute
        {
            _locator = Locator.tag("ul").withDescendant(
                    Locators.descriptionEdit(description));
            return this;
        }

        public CategoryEditRowFinder withEmptyDescription()   // finds the row based on key column's 'title' attribute
        {
            _locator = Locator.tag("ul").withDescendant(
                    Locators.descriptionEdit().withAttribute("value", ""));
            return this;
        }

        @Override
        protected CategoryEditRow construct(WebElement el, WebDriver driver)
        {
            return new CategoryEditRow(el, driver);
        }

        @Override
        protected Locator locator()
        {
            return _locator;
        }
    }
}