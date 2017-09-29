package org.labkey.test.components.snd;

import org.labkey.test.Locator;
import org.labkey.test.components.WebDriverComponent;
import org.labkey.test.components.html.Checkbox;
import org.labkey.test.components.html.Input;
import org.labkey.test.components.html.SelectWrapper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import static org.labkey.test.components.html.Input.Input;

public class AttributeGridRow extends WebDriverComponent<AttributeGridRow.ElementCache>
{
    final WebElement _el;
    final WebDriver _driver;

    public AttributeGridRow(WebElement element, WebDriver driver)
    {
        _el = element;          // componentElement will the a TR
        _driver = driver;
    }

    static public AttributeGridRowFinder finder(AttributesGrid grid)
    {
        return new AttributeGridRowFinder(grid);
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

    public AttributeGridRow setDataType(String dataType)
    {
        elementCache().dataTypeSelect.selectByVisibleText(dataType);
        return this;
    }

    public AttributeGridRow setLabel(String dataType)
    {
        elementCache().labelInput.set(dataType);
        return this;
    }

    public AttributeGridRow setMin(int min)
    {
        elementCache().minInput.set(Integer.toString(min));
        return this;
    }

    public AttributeGridRow setMax(int min)
    {
        elementCache().maxInput.set(Integer.toString(min));
        return this;
    }

    public AttributeGridRow setDefault(String defaultValue)
    {
        elementCache().defaultValueInput.set(defaultValue);
        return this;
    }

    public AttributeGridRow selectOrder(String order)   // values: "Move Up", "Move Down"
    {
        elementCache().orderSelect.selectByVisibleText(order);
        return this;
    }

    public AttributeGridRow setRequired(boolean required)   // values: "Move Up", "Move Down"
    {
        elementCache().requiredBox.set(required);
        return this;
    }

    public AttributeGridRow setRedactedText(String text)
    {
        elementCache().redactedTextInput.set(text);
        return this;
    }

    protected ElementCache newElementCache()
    {
        return new ElementCache();
    }

    protected class ElementCache extends WebDriverComponent.ElementCache
    {
        Select lookupKeySelect = SelectWrapper.Select(Locator.xpath("//div/select[contains(@name, '_lookupKey')]"))
                .findWhenNeeded(getComponentElement());
        Select dataTypeSelect = SelectWrapper.Select(Locator.xpath("//div/select[contains(@name, '_rangeURI')]"))
                .findWhenNeeded(getComponentElement());
        Input labelInput = Input(Locator.xpath("//div/input[contains(@name, '_label')]"), getDriver())
                .findWhenNeeded(getComponentElement());
        Input minInput = Input(Locator.xpath("//div/input[contains(@name, '_min')]"), getDriver()
        ).findWhenNeeded(getComponentElement());
        Input maxInput = Input(Locator.xpath("//div/input[contains(@name, '_max')]"), getDriver())
                .findWhenNeeded(getComponentElement());
        Input defaultValueInput = Input(Locator.xpath("//div/input[contains(@name, 'defaultValue')]"), getDriver())
                .findWhenNeeded(getComponentElement());
        Select orderSelect = SelectWrapper.Select(Locator.xpath("//div/select[contains(@name, '_sortOrder')]"))
                .findWhenNeeded(getComponentElement());
        org.labkey.test.components.html.Checkbox requiredBox = Checkbox.Checkbox(Locator.xpath("//span/input[contains(@name, '_required')]"))
                .findWhenNeeded(getComponentElement());
        Input redactedTextInput = Input(Locator.xpath("//div/input[contains(@name, '_redactedText')]"), getDriver())
                .findWhenNeeded(getComponentElement());
    }

    public static class AttributeGridRowFinder extends WebDriverComponent.WebDriverComponentFinder<AttributeGridRow, AttributeGridRowFinder>
    {
        private Locator _locator;
        private AttributesGrid _grid;

        private AttributeGridRowFinder(AttributesGrid grid)
        {
            super(grid.getDriver());
            _grid = grid;
        }

        public AttributeGridRowFinder withKey(String key)   // finds the row based on key column's 'title' attribute
        {
            _locator = Locator.tag("tbody").childTag("tr").withDescendant(
                    Locator.tagWithClass("div", "input-row").withAttribute("title", key));
            return this;
        }

        @Override
        protected AttributeGridRow construct(WebElement el, WebDriver driver)
        {
            return new AttributeGridRow(el, driver);
        }

        @Override
        protected Locator locator()
        {
            return _locator;
        }
    }
}