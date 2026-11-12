package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.pages.BasePage;
import com.enterprise.mobile.pages.locators.android.MeeshoListingPageLocators;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import java.util.Comparator;
import java.util.List;

/**
 * Catalog grid ("TROUSERS" listing). Product cards are a virtualised
 * RecyclerView, so the visible cards are read from the DOM, ordered by their
 * on-screen position and then selected by index.
 */
public class MeeshoListingPage extends BasePage {

    private static final By PRODUCT_CARD =
            AppiumBy.id("com.meesho.supply:id/item_catalog_card_optimised");
    private static final By CATALOG_NAME =
            AppiumBy.id("com.meesho.supply:id/catalog_name");
    private static final By PRICE =
            AppiumBy.id("com.meesho.supply:id/price");

    private final MeeshoListingPageLocators locators;

    public MeeshoListingPage(AppiumDriver driver) {
        super(driver);
        locators = new MeeshoListingPageLocators();
        PageFactory.initElements(decorator, locators);
    }

    @Override
    public boolean isLoaded() {
        return awaitAnyDisplayed(20, locators.catalogRecycler);
    }

    public MeeshoListingPage scrollDown(int times) {
        for (int i = 0; i < times; i++) {
            swipeSafely();
        }
        return this;
    }

    /**
     * UiAutomator occasionally rejects a gesture ("injectEventSync") while the
     * grid is re-rendering; retrying the same swipe recovers without failing the
     * scenario.
     */
    private void swipeSafely() {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                swipeUp();
                return;
            } catch (RuntimeException e) {
                log.warn("Swipe failed (attempt {}): {}", attempt + 1, e.getMessage());
                sleep(150);
            }
        }
    }

    /**
     * Opens the product card at the given zero-based index of the catalogue.
     * The grid renders only a few cards at a time, so the listing is scrolled
     * the minimum number of batches needed to bring the requested card into
     * view, and the card is then clicked by its position in that window.
     */
    public MeeshoProductPage openProductAtIndex(int index) {
        dumpPageSource("listing-before-product");
        int skipped = 0;
        List<WebElement> cards = catalogueCards();
        while (skipped + cards.size() <= index) {
            skipped += cards.size();
            swipeSafely();
            cards = catalogueCards();
            if (cards.isEmpty()) {
                break;
            }
        }
        int position = index - skipped;
        if (position < 0 || position >= cards.size()) {
            throw new IllegalStateException(
                    "Product index " + index + " is not available in the listing");
        }
        WebElement card = cards.get(position);
        log.info("Opening product index {} of {} catalogue cards (scrolled past {}): {}",
                index, cards.size(), skipped, nameOf(card));
        clickForcefully(card);
        MeeshoProductPage product = new MeeshoProductPage(driver);
        if (!product.isLoaded()) {
            // The tap did not open the product: go back to the listing and try
            // the same index once more.
            log.warn("Product index {} did not open; returning to the listing", index);
            driver.navigate().back();
            sleep(200);
            List<WebElement> retry = catalogueCards();
            if (retry.size() > position) {
                clickForcefully(retry.get(position));
                product = new MeeshoProductPage(driver);
            }
        }
        return product;
    }

    /** All rendered product cards in catalogue order. */
    private List<WebElement> catalogueCards() {
        return driver.findElements(PRODUCT_CARD);
    }

    public int visibleProductCount() {
        return visibleCards().size();
    }

    private List<WebElement> visibleCards() {
        return catalogueCards().stream()
                .filter(this::isOnScreen)
                .sorted(Comparator
                        .comparingInt((WebElement e) -> e.getLocation().getY())
                        .thenComparingInt(e -> e.getLocation().getX()))
                .toList();
    }

    private boolean isOnScreen(WebElement element) {
        try {
            Dimension size = element.getSize();
            return size.getWidth() > 0 && size.getHeight() > 20 && element.isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private String nameOf(WebElement card) {
        try {
            return card.findElement(CATALOG_NAME).getText();
        } catch (RuntimeException e) {
            return "<unknown>";
        }
    }

    private void dumpPageSource(String tag) {
        try {
            java.nio.file.Path dir = java.nio.file.Paths.get("target", "screenshots", "debug");
            java.nio.file.Files.createDirectories(dir);
            String name = tag + "-" + System.currentTimeMillis() + ".xml";
            java.nio.file.Files.write(dir.resolve(name),
                    driver.getPageSource().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            log.info("Page source dumped to target/screenshots/debug/{}", name);
        } catch (Exception e) {
            log.warn("Could not dump the page source: {}", e.getMessage());
        }
    }
}
