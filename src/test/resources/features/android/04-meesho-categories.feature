@android @meesho @categories
Feature: Meesho Supply categories browsing
  Walk the Categories tab from the catalogue to the Cargos/Trousers listing,
  open the product, scroll its images, pick the size and complete the order
  (price details, Buy Now, review page and payment options).

  Scenario: Browse Categories to a Cargos/Trousers product and verify the complete order flow
    Given the Meesho Supply app is launched and ready
    When the user opens the Categories section from the footer
    Then the Categories screen should be displayed
    And the category catalogue should list the top-level categories
    When the user opens the Men category
    Then the Bottom Wear section should be displayed
    When the user opens Cargos and Trousers
    Then the Cargos and Trousers listing should be displayed
    When the user opens product number 7 from the listing
    Then the product details page should be displayed
    When the user opens the product images in full screen
    Then the full screen image gallery should be displayed
    When the user scrolls the images three times to the right and once to the left
    And the user exits the full screen gallery
    Then the product details page should be displayed
    When the user scrolls the product page slightly to the Select Size section
    Then the Select Size section should be displayed with the available sizes
    And the user selects size 32
    Then size 32 should be selected and mandatory for the order
    When the user opens the price details dialog from the info icon
    Then the price details dialog should show MRP, product price, discount and final price
    When the user captures the final price and closes the price details dialog
    Then the captured final price should match the price shown on the product page
    When the user taps Buy Now
    Then the total price in the select preferences dialog should match the product page price
    Then the Review your order page should be displayed with the address and price details
    And the three prices on the review page should be the same
    When the user taps Continue on the review page
    Then the Payment Method page should be displayed
    And the Cash on Delivery and Pay Online options with their prices should be displayed
