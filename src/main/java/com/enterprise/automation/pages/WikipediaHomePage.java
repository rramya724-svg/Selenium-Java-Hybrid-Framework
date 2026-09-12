package com.enterprise.automation.pages;

import org.openqa.selenium.By;

import java.util.List;

/**
 * Page object for the Wikipedia multilingual portal.
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public class WikipediaHomePage extends BasePage {

    /* ----------------------------- LOCATORS ----------------------------- */

    private static final By CENTRAL_LOGO = By.cssSelector(".central-featured-logo");
    private static final By SEARCH_INPUT = By.id("searchInput");
    private static final By SEARCH_SUBMIT = By.cssSelector("button.pure-button-primary-progressive");
    private static final By LANGUAGE_TILES = By.cssSelector(".central-featured-lang strong");
    private static final By LANGUAGE_DROPDOWN = By.id("searchLanguage");

    private static final int LOGO_TIMEOUT_SECONDS = 15;

    /* ----------------------------- ACTIONS ------------------------------ */

    /**
     * Opens the Wikipedia portal.
     *
     * @param url the address configured in the test data workbook
     * @return this page object, enabling a fluent call chain
     */
    public WikipediaHomePage openHomePage(String url) {
        navigateTo(url);
        return this;
    }

    /**
     * @return whether the central Wikipedia globe logo rendered
     */
    public boolean isHomePageLoaded() {
        return isElementDisplayed(CENTRAL_LOGO, LOGO_TIMEOUT_SECONDS);
    }

    /**
     * @return whether the portal search input is available
     */
    public boolean isSearchInputDisplayed() {
        return isElementDisplayed(SEARCH_INPUT, LOGO_TIMEOUT_SECONDS);
    }

    /**
     * @return the names of the ten featured languages shown around the globe
     */
    public List<String> getFeaturedLanguages() {
        return getAllElementTexts(LANGUAGE_TILES);
    }

    /**
     * Searches Wikipedia for the supplied term.
     *
     * @param searchTerm the article to look for
     */
    public void searchFor(String searchTerm) {
        typeText(SEARCH_INPUT, searchTerm);
        clickElement(SEARCH_SUBMIT);
        logger.info("Wikipedia search submitted for [{}]", searchTerm);
    }

    /**
     * @return whether the search language selector rendered
     */
    public boolean isLanguageSelectorDisplayed() {
        return isElementDisplayed(LANGUAGE_DROPDOWN, LOGO_TIMEOUT_SECONDS);
    }
}
