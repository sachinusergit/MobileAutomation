package com.enterprise.mobile.bdd.context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Keeps track of the page the flow is on, of the verifications that are done
 * and of the ones that are still open. The snapshot is logged after every step
 * so a failure always shows where the execution stands and what is left.
 */
public final class VerificationTracker {

    /** The checks of the Meesho journey, in the order they have to happen. */
    public static final String APP_LAUNCHED = "app launched";
    public static final String CATEGORIES_SCREEN = "categories screen";
    public static final String CATEGORY_RAIL = "category rail";
    public static final String MEN_BOTTOM_WEAR = "men / bottom wear";
    public static final String LISTING = "cargos/trousers listing";
    public static final String PRODUCT_DETAILS = "product details page";
    public static final String GALLERY = "full screen gallery and image scroll";
    public static final String SIZE = "select size section and selected size";
    public static final String PRICE_DETAILS = "circled i price details";
    public static final String BUY_NOW = "buy now and select preferences sheet";
    public static final String REVIEW = "review order page and its three prices";
    public static final String PAYMENT = "payment method page";

    private static final Map<String, Boolean> CHECKS = new LinkedHashMap<>();

    private static String currentPage = "not started";

    private VerificationTracker() {
    }

    /** Records the page the flow is on. */
    public static synchronized void page(String page) {
        currentPage = page;
    }

    public static synchronized String currentPage() {
        return currentPage;
    }

    /** Marks a check as verified. */
    public static synchronized void verified(String check) {
        CHECKS.put(check, Boolean.TRUE);
    }

    /** Registers the checks that still have to happen. */
    public static synchronized void pending(String... checks) {
        for (String check : checks) {
            CHECKS.putIfAbsent(check, Boolean.FALSE);
        }
    }

    /** Clears the state for a new scenario. */
    public static synchronized void reset() {
        CHECKS.clear();
        currentPage = "not started";
    }

    /** @return one line with the page, the finished and the open checks */
    public static synchronized String snapshot() {
        String done = CHECKS.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));
        String open = CHECKS.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));
        return "PAGE [" + currentPage + "] | VERIFIED [" + done + "] | PENDING [" + open + "]";
    }
}
