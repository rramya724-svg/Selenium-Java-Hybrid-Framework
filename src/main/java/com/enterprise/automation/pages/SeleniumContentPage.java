package com.enterprise.automation.pages;

import org.openqa.selenium.By;

public class SeleniumContentPage extends BasePage {
    private static final By PAGE_HEADING = By.cssSelector("main h1, .td-content h1, h1");
    private static final int HEADING_TIMEOUT_SECONDS = 15;

    public SeleniumContentPage openPage(String url) {
        navigateTo(url);
        return this;
    }

    public boolean isPageHeadingDisplayed() {
        return isElementDisplayed(PAGE_HEADING, HEADING_TIMEOUT_SECONDS);
    }

    public String getPageHeading() {
        return getElementText(PAGE_HEADING);
    }
}
