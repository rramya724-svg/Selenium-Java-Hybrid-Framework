package com.enterprise.automation.pages;

import org.openqa.selenium.By;

/**
 * Page object for the official Selenium project website.
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public class SeleniumHomePage extends BasePage {

    /* ----------------------------- LOCATORS ----------------------------- */

    private static final By SITE_LOGO = By.cssSelector("a.navbar-brand img, img[alt*='Selenium']");
    private static final By DOCUMENTATION_LINK = By.cssSelector("a[href*='/documentation']");
    private static final By DOWNLOADS_LINK = By.cssSelector("a[href*='/downloads']");
    private static final By PROJECTS_MENU = By.cssSelector("a[href*='/projects']");
    private static final By HERO_HEADING = By.cssSelector("h1, .display-4");

    private static final int LOGO_TIMEOUT_SECONDS = 15;

    /* ----------------------------- ACTIONS ------------------------------ */

    /**
     * Opens the Selenium project home page.
     *
     * @param url the address configured in the test data workbook
     * @return this page object, enabling a fluent call chain
     */
    public SeleniumHomePage openHomePage(String url) {
        navigateTo(url);
        return this;
    }

    /**
     * @return whether the Selenium logo rendered in the navigation bar
     */
    public boolean isHomePageLoaded() {
        return isElementDisplayed(SITE_LOGO, LOGO_TIMEOUT_SECONDS);
    }

    /**
     * @return whether the Documentation entry point rendered
     */
    public boolean isDocumentationLinkDisplayed() {
        return isElementDisplayed(DOCUMENTATION_LINK, LOGO_TIMEOUT_SECONDS);
    }

    /**
     * @return whether the Downloads entry point rendered
     */
    public boolean isDownloadsLinkDisplayed() {
        return isElementDisplayed(DOWNLOADS_LINK, LOGO_TIMEOUT_SECONDS);
    }

    /**
     * @return whether the Projects menu rendered
     */
    public boolean isProjectsMenuDisplayed() {
        return isElementDisplayed(PROJECTS_MENU, LOGO_TIMEOUT_SECONDS);
    }

    /**
     * @return the main hero heading text shown on the landing page
     */
    public String getHeroHeading() {
        return getElementText(HERO_HEADING);
    }
}
