package com.enterprise.automation.pages;

import org.openqa.selenium.By;

public class AmazonHomePage extends BasePage {
    private static final By SITE_LOGO = By.id("nav-logo-sprites");
    private static final By SEARCH_BOX = By.id("twotabsearchtextbox");
    private static final By SEARCH_SUBMIT_BUTTON = By.id("nav-search-submit-button");
    private static final By ACCOUNT_MENU = By.id("nav-link-accountList");
    private static final By CART_LINK = By.id("nav-cart");
    private static final By SHOP_BY_CATEGORY_MENU = By.id("nav-hamburger-menu");
    private static final int LOGO_TIMEOUT_SECONDS = 15;

    public AmazonHomePage openHomePage(String url) {
        navigateTo(url);
        return this;
    }

    public boolean isHomePageLoaded() {
        return isElementDisplayed(SITE_LOGO, LOGO_TIMEOUT_SECONDS);
    }

    public boolean isSearchBoxDisplayed() {
        return isElementDisplayed(SEARCH_BOX, LOGO_TIMEOUT_SECONDS);
    }

    public boolean isHeaderNavigationDisplayed() {
        return isElementDisplayed(ACCOUNT_MENU, LOGO_TIMEOUT_SECONDS)
                && isElementDisplayed(CART_LINK, LOGO_TIMEOUT_SECONDS);
    }

    public void searchFor(String searchTerm) {
        typeText(SEARCH_BOX, searchTerm);
        clickElement(SEARCH_SUBMIT_BUTTON);
        logger.info("Amazon search submitted for [{}]", searchTerm);
    }

    public void openCategoryMenu() {
        clickElement(SHOP_BY_CATEGORY_MENU);
    }
}
