package com.enterprise.automation.pages;

import org.openqa.selenium.By;

public class SeleniumHomePage extends BasePage {
    private static final By SITE_LOGO = By.cssSelector("a.navbar-brand img, img[alt*='Selenium']");
    private static final By DOCUMENTATION_LINK = By.cssSelector("a[href*='/documentation']");
    private static final By DOWNLOADS_LINK = By.cssSelector("a[href*='/downloads']");
    private static final By PROJECTS_MENU = By.cssSelector("a[href*='/projects']");
    private static final By HERO_HEADING = By.cssSelector("h1, .display-4");
    private static final int LOGO_TIMEOUT_SECONDS = 15;

    public SeleniumHomePage openHomePage(String url) {
        navigateTo(url);
        return this;
    }

    public boolean isHomePageLoaded() {
        return isElementDisplayed(SITE_LOGO, LOGO_TIMEOUT_SECONDS);
    }

    public boolean isDocumentationLinkDisplayed() {
        return isElementDisplayed(DOCUMENTATION_LINK, LOGO_TIMEOUT_SECONDS);
    }

    public boolean isDownloadsLinkDisplayed() {
        return isElementDisplayed(DOWNLOADS_LINK, LOGO_TIMEOUT_SECONDS);
    }

    public boolean isProjectsMenuDisplayed() {
        return isElementDisplayed(PROJECTS_MENU, LOGO_TIMEOUT_SECONDS);
    }

    public String getHeroHeading() {
        return getElementText(HERO_HEADING);
    }
}
