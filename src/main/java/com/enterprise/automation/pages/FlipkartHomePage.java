package com.enterprise.automation.pages;

import org.openqa.selenium.By;

/**
 * Page object for the Flipkart landing page.
 *
 * <p>
 * Provides navigation and common validation/actions for the Flipkart
 * landing page.
 * </p>
 *
 * @author Lalith Kumar BV
 * @version 2.0
 */
public class FlipkartHomePage extends BasePage {

    /* ----------------------------- LOCATORS ----------------------------- */

    private static final By SITE_LOGO =
            By.cssSelector(
                    "a[title='Flipkart'], img[title='Flipkart']"
            );

    private static final By SEARCH_BOX =
            By.cssSelector(
                    "input[name='q'], " +
                    "input[title='Search for Products, Brands and More']"
            );

    private static final By LOGIN_POPUP_CLOSE_BUTTON =
            By.xpath(
                    "//button[normalize-space(text())='\u2715']" +
                    " | //span[normalize-space(text())='\u2715']"
            );

    private static final By CART_LINK =
            By.xpath(
                    "//a[contains(@href,'/viewcart')]"
            );

    private static final int LOGO_TIMEOUT_SECONDS = 15;

    private static final int POPUP_TIMEOUT_SECONDS = 5;

    /* ----------------------------- ACTIONS ------------------------------ */

    /**
     * Opens the Flipkart home page.
     *
     * @param url URL configured in test data
     * @return current page object
     */
    public FlipkartHomePage openHomePage(String url) {

        logger.info(
                "Opening Flipkart home page: [{}]",
                url
        );

        navigateTo(url);

        logger.info(
                "Flipkart navigation command completed. Current URL: [{}]",
                getCurrentUrl()
        );

        return this;
    }

    /**
     * Closes the Flipkart login popup when present.
     *
     * @return true when popup was dismissed, otherwise false
     */
    public boolean dismissLoginPopupIfPresent() {

        logger.debug(
                "Checking for Flipkart login popup"
        );

        if (isElementDisplayed(
                LOGIN_POPUP_CLOSE_BUTTON,
                POPUP_TIMEOUT_SECONDS
        )) {

            clickElement(
                    LOGIN_POPUP_CLOSE_BUTTON
            );

            logger.info(
                    "Flipkart login popup dismissed successfully"
            );

            return true;
        }

        logger.debug(
                "Flipkart login popup was not displayed"
        );

        return false;
    }

    /**
     * Checks whether the Flipkart logo is displayed.
     *
     * @return true when the logo is visible
     */
    public boolean isHomePageLoaded() {

        boolean loaded =
                isElementDisplayed(
                        SITE_LOGO,
                        LOGO_TIMEOUT_SECONDS
                );

        logger.info(
                "Flipkart home page loaded: [{}]",
                loaded
        );

        return loaded;
    }

    /**
     * Checks whether the search box is displayed.
     *
     * @return true when search box is visible
     */
    public boolean isSearchBoxDisplayed() {

        boolean displayed =
                isElementDisplayed(
                        SEARCH_BOX,
                        LOGO_TIMEOUT_SECONDS
                );

        logger.info(
                "Flipkart search box displayed: [{}]",
                displayed
        );

        return displayed;
    }

    /**
     * Checks whether the cart link is displayed.
     *
     * @return true when cart link is visible
     */
    public boolean isCartLinkDisplayed() {

        boolean displayed =
                isElementDisplayed(
                        CART_LINK,
                        LOGO_TIMEOUT_SECONDS
                );

        logger.info(
                "Flipkart cart link displayed: [{}]",
                displayed
        );

        return displayed;
    }

    /**
     * Searches for a product using the Flipkart search box.
     *
     * @param searchTerm product/search text
     */
    public void searchFor(String searchTerm) {

        if (searchTerm == null ||
                searchTerm.isBlank()) {

            throw new IllegalArgumentException(
                    "Search term cannot be null or blank"
            );
        }

        logger.info(
                "Searching Flipkart for [{}]",
                searchTerm
        );

        typeText(
                SEARCH_BOX,
                searchTerm
        );

        com.enterprise.automation.utilities.ActionsUtility
                .pressKey(
                        org.openqa.selenium.Keys.ENTER
                );

        logger.info(
                "Flipkart search submitted for [{}]",
                searchTerm
        );
    }
}