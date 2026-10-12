package com.enterprise.automation.pages;

import com.enterprise.automation.utilities.WaitUtility;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import java.util.List;

public class WikipediaHomePage extends BasePage {
    private static final By CENTRAL_LOGO = By.cssSelector(".central-featured-logo");
    private static final By SEARCH_INPUT = By.id("searchInput");
    private static final By SEARCH_SUBMIT = By.cssSelector("button.pure-button-primary-progressive");
    private static final By LANGUAGE_TILES = By.cssSelector(".central-featured-lang strong");
    private static final By LANGUAGE_DROPDOWN = By.id("searchLanguage");
    private static final int LOGO_TIMEOUT_SECONDS = 15;
    private static final String ARTICLE_PATH_FRAGMENT = "/wiki/";

    public WikipediaHomePage openHomePage(String url) {
        navigateTo(url);
        return this;
    }

    public boolean isHomePageLoaded() {
        return isElementDisplayed(CENTRAL_LOGO, LOGO_TIMEOUT_SECONDS);
    }

    public boolean isSearchInputDisplayed() {
        return isElementDisplayed(SEARCH_INPUT, LOGO_TIMEOUT_SECONDS);
    }

    public List<String> getFeaturedLanguages() {
        return getAllElementTexts(LANGUAGE_TILES);
    }

    public void searchFor(String searchTerm) {
        typeText(SEARCH_INPUT, searchTerm);
        clickElement(SEARCH_SUBMIT);
        logger.info("Wikipedia search submitted for [{}]", searchTerm);
    }

    public boolean isLanguageSelectorDisplayed() {
        return isElementDisplayed(LANGUAGE_DROPDOWN, LOGO_TIMEOUT_SECONDS);
    }

    public void searchWithEnterKey(String searchTerm) {
        typeText(SEARCH_INPUT, searchTerm + Keys.ENTER);
        WaitUtility.waitForUrlContains(ARTICLE_PATH_FRAGMENT);
        logger.info("Wikipedia search submitted with ENTER for [{}]", searchTerm);
    }
}
