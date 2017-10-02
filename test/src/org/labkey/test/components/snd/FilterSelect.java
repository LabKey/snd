package org.labkey.test.components.snd;

import org.labkey.test.Locator;
import org.labkey.test.WebDriverWrapper;
import org.labkey.test.components.WebDriverComponent;
import org.labkey.test.components.html.Input;
import org.labkey.test.pages.LabKeyPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
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


    public boolean isOpen()
    {   // this assumes any buttons/items exist
        WebElement searchContainer = Locator.tagWithClassContaining("div", "data-search__container")
                .child(Locator.tag("div").child(Locator.tag("div").child("button")))
                .findElementOrNull(getComponentElement());
        return searchContainer != null && searchContainer.isDisplayed();
    }

    public FilterSelect open()
    {
        if (!isOpen())
            elementCache().input.getComponentElement().click();
        getWrapper().waitFor(()-> isOpen(), 1000);
        return this;
    }

    public FilterSelect close()
    {
        if (isOpen())
        {
            // click just to the right of the input element
            getWrapper().clickAt(elementCache().input.getComponentElement(), -10, 180, 0);
        }
        getWrapper().waitFor(()-> !isOpen(), 1000);
        return this;
    }

    public FilterSelect setFilter(String value)
    {
        elementCache().input.set(value);
        return this;
    }

    public FilterSelect selectItem(String itemText)
    {
        open();
        WebElement item = elementCache().option(itemText);
        getWrapper().scrollIntoView(item);
        getWrapper().fireEvent(item, WebDriverWrapper.SeleniumEvent.mouseover);
        if (Locator.tagWithClass("i", "fa fa-check").findElementOrNull(item) == null)
            item.click();
        return this;
    }

    public FilterSelect deselectItem(String itemText)
    {
        WebElement item = elementCache().option(itemText);
        getWrapper().scrollIntoView(item);
        if (Locator.tagWithClass("i", "fa fa-check").findElementOrNull(item) != null)
            item.click();
        return this;
    }

    public List<String> getSelections()
    {
        close();
        WebElement selectionsContainer = Locator.tagWithClass("div", "data-search__row_selected")
                .childTag("div")
                .findElementOrNull(getComponentElement());
        if (null==selectionsContainer)
            return new ArrayList<>();

        return Arrays.asList(selectionsContainer.getText().split(","));
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
                    .withChild(Locator.tagContainingText("div", text))
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