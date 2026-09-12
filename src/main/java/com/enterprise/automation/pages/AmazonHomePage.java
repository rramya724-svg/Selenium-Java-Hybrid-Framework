package com.enterprise.automation.pages;

import org.openqa.selenium.By;

/**
 * Page object for the Amazon India landing page.
 *
 * <p>Locators are declared as private static final {@link By} constants rather
 * than {@code @FindBy} proxies: a {@code By} is immutable and therefore
 * completely safe to share across parallel threads, whereas a PageFactory proxy
 * is bound to the driver that initialised it.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public class AmazonHomePage extends BasePage {

    /* ----------------------------- LOCATORS ----------------------------- */

    private static final By SITE_LOGO = By.id("nav-logo-sprites");
    private static final By SEARCH_BOX = By.id("twotabsearchtextbox");
    private static final By SEARCH_SUBMIT_BUTTON = By.id("nav-search-submit-button");
    private static final By ACCOUNT_MENU = By.id("nav-link-accountList");
    private static final By CART_LINK = By.id("nav-cart");
    private static final By SHOP_BY_CATEGORY_MENU = By.id("nav-hamburger-menu");

    /** Short wait used only for optional presence checks. */
    private static final int LOGO_TIMEOUT_SECONDS = 15;

    /* ----------------------------- ACTIONS ------------------------------ */

    /**
     * Opens the Amazon India home page.
     *
     * @param url the address configured in the test data workbook
     * @return this page object, enabling a fluent call chain
     */
    public AmazonHomePage openHomePage(String url) {
        navigateTo(url);
        return this;
    }

    /**
     * @return whether the Amazon header logo rendered, confirming the page loaded
     */
    public boolean isHomePageLoaded() {
        return isElementDisplayed(SITE_LOGO, LOGO_TIMEOUT_SECONDS);
    }

    /**
     * @return whether the primary search box is available to the user
     */
    public boolean isSearchBoxDisplayed() {
        return isElementDisplayed(SEARCH_BOX, LOGO_TIMEOUT_SECONDS);
    }

    /**
     * @return whether the account and cart controls rendered in the header
     */
    public boolean isHeaderNavigationDisplayed() {
        return isElementDisplayed(ACCOUNT_MENU, LOGO_TIMEOUT_SECONDS)
                && isElementDisplayed(CART_LINK, LOGO_TIMEOUT_SECONDS);
    }

    /**
     * Types a term into the search box and submits it.
     *
     * @param searchTerm the term to look for
     */
    public void searchFor(String searchTerm) {
        typeText(SEARCH_BOX, searchTerm);
        clickElement(SEARCH_SUBMIT_BUTTON);
        logger.info("Amazon search submitted for [{}]", searchTerm);
    }

    /** Opens the "All" category flyout menu. */
    public void openCategoryMenu() {
        clickElement(SHOP_BY_CATEGORY_MENU);
    }
}
