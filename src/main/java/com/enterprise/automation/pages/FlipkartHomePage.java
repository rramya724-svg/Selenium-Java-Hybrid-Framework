package com.enterprise.automation.pages;

import org.openqa.selenium.By;

/**
 * Page object for the Flipkart landing page.
 *
 * <p>Flipkart shows a dismissible login modal on first load, so the page object
 * exposes an explicit {@link #dismissLoginPopupIfPresent()} step rather than
 * hiding the behaviour inside another action.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public class FlipkartHomePage extends BasePage {

    /* ----------------------------- LOCATORS ----------------------------- */

    private static final By SITE_LOGO = By.cssSelector("a[title='Flipkart'], img[title='Flipkart']");
    private static final By SEARCH_BOX = By.cssSelector("input[name='q'], input[title='Search for Products, Brands and More']");
    /** Flipkart renders the modal close control as a multiplication-X glyph (U+2715). */
    private static final By LOGIN_POPUP_CLOSE_BUTTON =
            By.xpath("//button[normalize-space(text())='\u2715'] | //span[normalize-space(text())='\u2715']");
    private static final By CART_LINK = By.xpath("//a[contains(@href,'/viewcart')]");

    private static final int LOGO_TIMEOUT_SECONDS = 15;
    private static final int POPUP_TIMEOUT_SECONDS = 5;

    /* ----------------------------- ACTIONS ------------------------------ */

    /**
     * Opens the Flipkart home page.
     *
     * @param url the address configured in the test data workbook
     * @return this page object, enabling a fluent call chain
     */
    public FlipkartHomePage openHomePage(String url) {
        navigateTo(url);
        return this;
    }

    /**
     * Closes the login modal when it appears. Absence of the modal is a valid
     * state, so this method never fails the test.
     *
     * @return whether a modal was actually dismissed
     */
    public boolean dismissLoginPopupIfPresent() {
        if (isElementDisplayed(LOGIN_POPUP_CLOSE_BUTTON, POPUP_TIMEOUT_SECONDS)) {
            clickElement(LOGIN_POPUP_CLOSE_BUTTON);
            logger.info("Flipkart login popup dismissed");
            return true;
        }
        logger.debug("Flipkart login popup was not displayed");
        return false;
    }

    /**
     * @return whether the Flipkart brand logo rendered
     */
    public boolean isHomePageLoaded() {
        return isElementDisplayed(SITE_LOGO, LOGO_TIMEOUT_SECONDS);
    }

    /**
     * @return whether the product search box is available
     */
    public boolean isSearchBoxDisplayed() {
        return isElementDisplayed(SEARCH_BOX, LOGO_TIMEOUT_SECONDS);
    }

    /**
     * @return whether the cart entry point rendered in the header
     */
    public boolean isCartLinkDisplayed() {
        return isElementDisplayed(CART_LINK, LOGO_TIMEOUT_SECONDS);
    }

    /**
     * Types a term into the search box and submits it with the Enter key.
     *
     * @param searchTerm the term to look for
     */
    public void searchFor(String searchTerm) {
        typeText(SEARCH_BOX, searchTerm);
        com.enterprise.automation.utilities.ActionsUtility.pressKey(org.openqa.selenium.Keys.ENTER);
        logger.info("Flipkart search submitted for [{}]", searchTerm);
    }
}
