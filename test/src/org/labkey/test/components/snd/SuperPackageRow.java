package org.labkey.test.components.snd;

import org.labkey.test.Locator;
import org.labkey.test.WebDriverWrapper;
import org.labkey.test.components.WebDriverComponent;
import org.labkey.test.components.html.BootstrapMenu;
import org.labkey.test.components.html.Input;
import org.labkey.test.pages.LabKeyPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.labkey.test.components.html.Input.Input;

public class SuperPackageRow extends WebDriverComponent<SuperPackageRow.ElementCache>
{
    final WebElement _el;
    final WebDriver _driver;

    public SuperPackageRow(WebElement element, WebDriver driver)
    {
        _el = element;
        _driver = driver;
    }

    static public SuperPackageRowFinder finder(WebDriver driver)
    {
        return new SuperPackageRowFinder(driver);
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

    public boolean isSelected()
    {
        return getComponentElement().getAttribute("class").contains("superpackage-selected-row");
    }

    public SuperPackageRow select()
    {
        if (!isSelected())
            newElementCache().desc.click();
        getWrapper().waitFor(()-> isSelected(),
                "row item never became selected",2000);
        return this;
    }

    public String getLabel()
    {
        return newElementCache().desc.getText();
    }

    public SuperPackageRow clickMenuItem(String menuText)
    {
       getWrapper().fireEvent(newElementCache().menuToggle, WebDriverWrapper.SeleniumEvent.mouseover);
       newElementCache().menuToggle.click();
       // wait for the menu to expand
       getWrapper().waitFor(()-> newElementCache().menuToggle.getAttribute("aria-expanded").equals("true"), 1000);
       Locator menuItem = Locator.tagWithAttribute("li", "role", "presentation")
               .child(Locator.tagWithAttribute("a", "role", "menuitem")
                       .containing(menuText));
       getWrapper().waitFor(()-> menuItem.findElement(getComponentElement()).isEnabled(), 2000);
       menuItem.findElement(getComponentElement()).click();
       // wait for the menu to collapse
       getWrapper().waitFor(()-> newElementCache().menuToggle.getAttribute("aria-expanded").equals("false"), 1000);
       return this;
    }

    protected ElementCache newElementCache()
    {
        return new ElementCache();
    }

    protected class ElementCache extends WebDriverComponent.ElementCache
    {
        public WebElement menuToggle = Locator.tagWithClassContaining("div", "dropdown")
                        .child(Locator.id("superpackage-actions"))
                        .findWhenNeeded(getComponentElement()).withTimeout(4000);

        public WebElement menuList = Locator.tagWithAttribute("ul", "role", "menu")
                .findWhenNeeded(getComponentElement()).withTimeout(1000);


        public  WebElement desc = Locators.desc.findWhenNeeded(getComponentElement()).withTimeout(4000);
    }

    public static class Locators
    {
        public static Locator.XPathLocator body = Locator.tagWithClassContaining("div", "superpackage_viewer__result");
        public static Locator.XPathLocator desc = Locator.tagWithClass("div", "pull-left");
    }

    public static class SuperPackageRowFinder extends WebDriverComponent.WebDriverComponentFinder<SuperPackageRow, SuperPackageRowFinder>
    {
        private Locator _locator;

        private SuperPackageRowFinder(WebDriver driver)
        {
            super(driver);
            _locator = Locators.body;
        }

        public SuperPackageRowFinder withDescription(String description)
        {
            _locator = Locators.body.withChild(Locators.desc.withText(description));
            return this;
        }

        public SuperPackageRowFinder withPartialDescription(String partialDescription)
        {
            _locator = Locators.body.withChild(Locators.desc.containing(partialDescription));
            return this;
        }

        @Override
        protected SuperPackageRow construct(WebElement el, WebDriver driver)
        {
            return new SuperPackageRow(el, driver);
        }

        @Override
        protected Locator locator()
        {
            return _locator;
        }
    }
}