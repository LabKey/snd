package org.labkey.test.components.snd;

import org.labkey.test.Locator;
import org.labkey.test.components.WebDriverComponent;
import org.labkey.test.components.html.Input;
import org.labkey.test.pages.LabKeyPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.labkey.test.components.html.Input.Input;

public class FilterSelect extends WebDriverComponent<FilterSelect.ElementCache>
{
    final WebElement _el;
    final WebDriver _driver;

    public FilterSelect(WebElement element, WebDriver driver)
    {
        _el = element;
        _driver = driver;
    }

    static public FilterSelectFinder finder(WebDriver driver)
    {
        return new FilterSelectFinder(driver);
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

    public FilterSelect open()
    {
        elementCache().input.getComponentElement().click();
        return this;
    }

    public FilterSelect setFilter(String value)
    {
        elementCache().input.set(value);
        return this;
    }

    public FilterSelect selectItem(String itemText)
    {
        WebElement item = elementCache().option(itemText);
        getWrapper().scrollIntoView(item);
        if (Locator.tagWithClass("i", "fa fa-check").findElementOrNull(item) != null)
            item.click();
        return this;
    }
    public FilterSelect deselectItem(String itemText)
    {
        WebElement item = elementCache().option(itemText);
        getWrapper().scrollIntoView(item);
        if (Locator.tagWithClass("i", "fa fa-check").findElementOrNull(item) == null)
            item.click();
        return this;
    }

    protected ElementCache newElementCache()
    {
        return new ElementCache();
    }

    protected class ElementCache extends WebDriverComponent.ElementCache
    {
        Input input = Input(Locator.tagWithName("input", "query-search-input"), getDriver()).findWhenNeeded(this);

        WebElement option(String text)
        {
            return Locator.tagWithClassContaining("button", "list-group-item")
                    .withChild(Locator.tagWithText("div", text))
                    .findElement(getComponentElement());
        }
        List<WebElement> options()
        {
            return Locator.tagWithClassContaining("button", "list-group-item").findElements(getComponentElement());
        }
    }

    public static class FilterSelectFinder extends WebDriverComponent.WebDriverComponentFinder<FilterSelect, FilterSelectFinder>
    {
        private Locator _locator;

        private FilterSelectFinder(WebDriver driver)
        {
            super(driver);
            _locator = Locator.tagWithClass("div", "query-search--container");
        }

        public FilterSelectFinder withName(String name)   // finds the row based on key column's 'title' attribute
        {
            _locator = Locator.tagWithClass("div", "query-search--container").withDescendant(
                    Locator.tagWithName("input", name));
            return this;
        }

        @Override
        protected FilterSelect construct(WebElement el, WebDriver driver)
        {
            return new FilterSelect(el, driver);
        }

        @Override
        protected Locator locator()
        {
            return _locator;
        }
    }
}