@android @meesho @e2e
Feature: Meesho Supply pricing and payment verification
  The size is verified in two ways: selected straight on the product details
  page, and selected in the Buy Now preferences sheet when Buy Now is pressed
  without a size. Both paths verify the circled "i" price details, the price in
  the preferences sheet, the three prices on the review page and the payment
  options.

  Scenario: Verify the price details after selecting the size on the product details page
    Given the Meesho Supply app is launched and ready
    And the user is signed in with the configured phone number
    And the user is on the product details page of product number 7
    When the user opens the product images in full screen
    Then the full screen image gallery should be displayed
    When the user scrolls the images three times to the right and once to the left
    And the user exits the full screen gallery
    Then the product details page should be displayed
    When the user scrolls the product page slightly to the Select Size section
    And the user selects the available size
    And the user opens the price details dialog from the info icon
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

  Scenario: Verify the size in the select preferences dialog when Buy Now is pressed without a size
    Given the Meesho Supply app is launched and ready
    And the user is signed in with the configured phone number
    And the user is on the product details page of product number 7
    When the user opens the product images in full screen
    Then the full screen image gallery should be displayed
    When the user scrolls the images three times to the right and once to the left
    And the user exits the full screen gallery
    Then the product details page should be displayed
    And the user opens the price details dialog from the info icon
    Then the price details dialog should show MRP, product price, discount and final price
    When the user captures the final price and closes the price details dialog
    Then the captured final price should match the price shown on the product page
    When the user taps Buy Now without selecting a size on the product page
    Then the size should be selected in the select preferences dialog box
    And the total price in the select preferences dialog should match the product page price
    Then the Review your order page should be displayed with the address and price details
    And the three prices on the review page should be the same
    When the user taps Continue on the review page
    Then the Payment Method page should be displayed
    And the Cash on Delivery and Pay Online options with their prices should be displayed
